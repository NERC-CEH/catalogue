package uk.ac.ceh.gateway.catalogue.metrics;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.jspecify.annotations.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.TimeConstants;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.time.Instant;
import java.util.*;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

@Profile("metrics")
@Slf4j
@Service
public class JDBCMetricsService implements MetricsService, ApplicationListener<ApplicationReadyEvent> {
    @NonNull private final Map<String, Set<String>> viewed;
    @NonNull private final Map<String, Set<String>> downloaded;
    @NonNull private final SimpleJdbcInsert viewInserter;
    @NonNull private final SimpleJdbcInsert downloadInserter;
    @NonNull private final JdbcTemplate jdbcTemplate;
    private long lastRun;
    private final DocumentRepository documentRepository;

    /**
     * Set once the tables and indexes are known to exist. {@code volatile} so the fast path in
     * {@link #ensureSchema} needs no lock; the slow path takes {@link #schemaLock} so two callers racing
     * after a recovery do not both run the DDL.
     */
    private volatile boolean schemaReady;
    private final Object schemaLock = new Object();

    // SQLite has no built-in datetime type, so we store dates as Unix timestamps (seconds since 1 Jan 1970).
    // bigint, not integer: SQLite's integer is 64-bit, but PostgreSQL's is 32-bit and would overflow in
    // January 2038 -- silently, since pgjdbc narrows the bound long to an int. bigint is still INTEGER
    // affinity on SQLite, and IF NOT EXISTS leaves the existing SQLite tables as they are.
    private static final String CREATE_STATEMENT = """
        CREATE TABLE IF NOT EXISTS %s (
            start_timestamp bigint NOT NULL,
            end_timestamp bigint NOT NULL,
            amount integer NOT NULL,
            document text NOT NULL,
            doc_title text NOT NULL,
            record_type text NOT NULL
        )
        """;
    /**
     * Every count read filters on {@code document}, and the table carried no index for its entire
     * lifetime, so each read was a full scan. In production that meant scanning a 1.1 GB SQLite file
     * across a CIFS mount — about 7 seconds per scan, twice per record page render, which is what left
     * 135 of 139 request threads sitting in {@code NativeDB.step}.
     *
     * <p>{@code amount} is in the index as well as {@code document}, making it a covering index for
     * {@link #TOTAL_STATEMENT}: the sum is computed from a contiguous index range without touching the
     * table at all. With {@code document} alone the row for each match still has to be fetched, and at
     * roughly a row per document per hour that is thousands of random reads across the network per
     * count.</p>
     *
     * <p>Creating this on an existing database is a one-off cost, proportional to the table size, paid
     * by {@link #ensureSchema}; the timing is logged. It is deliberately {@code IF NOT EXISTS} so restarts
     * are free.</p>
     */
    private static final String INDEX_STATEMENT =
        "CREATE INDEX IF NOT EXISTS idx_%1$s_document_amount ON %1$s (document, amount)";
    private static final String TOTAL_STATEMENT = "SELECT coalesce(sum(amount), 0) FROM %s WHERE document = ?";
    private static final String UPDATE_STATEMENT = "UPDATE %s SET doc_title = ?, record_type = ? WHERE document = ?";
    private static final String DISTINCT_DOCS_QUERY = "SELECT DISTINCT document FROM %s";
    private static final String COUNT_STATEMENT = "SELECT count(*) FROM %s WHERE document = ?";
    private static final String DELETE_STATEMENT = "DELETE FROM %s WHERE document = ?";
    private static final String VIEW_TABLE = "views";
    private static final String DOWNLOAD_TABLE = "downloads";

    /**
     * Separate caches per count, not one shared cache: {@link #totalViews} and {@link #totalDownloads}
     * take the same single {@code String} argument, so under the default key generator a shared cache
     * would give them the same key and one would serve the other's number.
     */
    public static final String VIEW_TOTALS_CACHE = "metrics-view-totals";
    public static final String DOWNLOAD_TOTALS_CACHE = "metrics-download-totals";

    /**
     * Deliberately does no I/O. The schema used to be created here, which meant an unreachable metrics
     * database failed bean creation and took the whole context down with it — Hikari's
     * {@code initialization-fail-timeout=-1} only makes building the pool lazy, it cannot help a
     * consumer that runs SQL while the context is starting. The DDL now runs from {@link #ensureSchema}.
     */
    public JDBCMetricsService(@NonNull DataSource dataSource,
                              DocumentRepository documentRepository) {
        log.info("Creating");
        this.documentRepository = documentRepository;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.viewInserter = new SimpleJdbcInsert(dataSource).withTableName(VIEW_TABLE);
        this.downloadInserter = new SimpleJdbcInsert(dataSource).withTableName(DOWNLOAD_TABLE);
        this.viewed = Collections.synchronizedMap(new HashMap<>());
        this.downloaded = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * First attempt at the schema, once the context is up. {@code ApplicationReadyEvent} is published
     * before Spring Boot flips readiness to {@code ACCEPTING_TRAFFIC}, so on a healthy database the
     * tables and indexes exist before Kubernetes routes any traffic to the pod.
     *
     * <p>{@link ApplicationListener} rather than {@code @EventListener}: this bean is proxied for
     * {@code @Cacheable}, and under JDK interface proxies (as in {@code MetricsCountCachingTest}) an
     * annotated method that is not on an interface cannot be invoked through the proxy, so the context
     * fails to start. Implementing the interface puts {@code onApplicationEvent} on the proxy whichever
     * proxy type is in use.</p>
     */
    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        initialiseSchemaOnStartup();
    }

    /** Also called directly by tests, which have no {@code ApplicationReadyEvent}. */
    public void initialiseSchemaOnStartup() {
        ensureSchema();
    }

    /**
     * Creates the tables and indexes if they are not already known to exist, and reports whether they
     * now do. Never throws for a database problem: an unreachable database is logged and left for the
     * next caller to retry, which is what lets the catalogue start while the metrics database is down —
     * the startup-failure policy stated in {@code application-metrics.properties}.
     *
     * <p>Called on startup and then lazily from every path that is not on the record-page render path
     * ({@link #syncDB}, {@link #updateDB}, the report and the admin checks), so a database that was down
     * at startup gets its schema as soon as it comes back, at the latest on the next hourly sync. The
     * totals reads are deliberately left out: they already return {@code null} on any
     * {@link DataAccessException}, including a missing table, and a second connection attempt per render
     * against a database that is down would only double the time the page waits.</p>
     *
     * <p>Once it has succeeded this is a single volatile read.</p>
     */
    public boolean ensureSchema() {
        if (schemaReady) {
            return true;
        }
        synchronized (schemaLock) {
            if (schemaReady) {
                return true;
            }
            try {
                List.of(VIEW_TABLE, DOWNLOAD_TABLE).forEach(table -> {
                    jdbcTemplate.execute(CREATE_STATEMENT.formatted(table));
                    val startedAt = System.currentTimeMillis();
                    jdbcTemplate.execute(INDEX_STATEMENT.formatted(table));
                    log.info("Ensured index on {}(document, amount) in {}ms", table, System.currentTimeMillis() - startedAt);
                });
                schemaReady = true;
            } catch (DataAccessException ex) {
                log.warn("Metrics database schema could not be ensured, will retry on next use: {}", ex.getMessage());
            }
            return schemaReady;
        }
    }

    @Override
    public void recordView(@NonNull String uuid, @NonNull String addr) {
        synchronized (viewed) {
            recordMetric(viewed, uuid, addr);
        }
    }

    @Override
    public void recordDownload(@NonNull String uuid, @NonNull String addr) {
        synchronized (downloaded) {
            recordMetric(downloaded, uuid, addr);
        }
    }

    /**
     * Cached because this is called while a record page renders, so an uncached read puts a query
     * against a SQLite database on a network share directly on the render critical path.
     *
     * <p>{@code unless} keeps an unavailable count out of the cache: {@link #totalAmount} returns
     * {@code null} when the database cannot be read, and Spring's cache manager stores nulls by
     * default, so without this a momentary failure would suppress the counter for the whole TTL
     * instead of recovering on the next request.</p>
     *
     * <p>Invalidation is by TTL alone, which is sound here in a way it would not be for record content:
     * {@link #syncDB} only writes hourly, so a total cannot change more often than that, and a slightly
     * stale view count on a page has no correctness consequence.</p>
     */
    @Override
    @Cacheable(cacheNames = VIEW_TOTALS_CACHE, unless = "#result == null")
    public @Nullable Integer totalViews(@NonNull String uuid) {
        return totalAmount(VIEW_TABLE, uuid);
    }

    /** Cached on the same terms as {@link #totalViews}, in its own cache to keep the keys apart. */
    @Override
    @Cacheable(cacheNames = DOWNLOAD_TOTALS_CACHE, unless = "#result == null")
    public @Nullable Integer totalDownloads(@NonNull String uuid) {
        return totalAmount(DOWNLOAD_TABLE, uuid);
    }

    @Override
    public boolean hasMetricsFor(@NonNull String uuid) {
        ensureSchema();
        return count(VIEW_TABLE, uuid) > 0 || count(DOWNLOAD_TABLE, uuid) > 0;
    }

    /**
     * Deletes by {@code document} from both tables independently: a record can have views without
     * downloads or vice versa, and the caller only wants to know whether anything was removed, not from
     * which table.
     */
    @Override
    public boolean deleteMetricsFor(@NonNull String uuid) {
        ensureSchema();
        int viewsDeleted = jdbcTemplate.update(DELETE_STATEMENT.formatted(VIEW_TABLE), uuid);
        int downloadsDeleted = jdbcTemplate.update(DELETE_STATEMENT.formatted(DOWNLOAD_TABLE), uuid);
        return viewsDeleted > 0 || downloadsDeleted > 0;
    }

    private int count(String table, String uuid) {
        Integer count = jdbcTemplate.queryForObject(COUNT_STATEMENT.formatted(table), Integer.class, uuid);
        return count == null ? 0 : count;
    }

    /**
     * Drains the in-memory counts under the lock and writes them outside it.
     *
     * <p>Previously the repository read and the insert-per-document in {@link #syncDBHelper} ran
     * while holding the same monitor as {@link #recordView} and {@link #recordDownload}. Against a
     * local SQLite file that was brief enough not to matter. Against a networked PostgreSQL it is a
     * site-wide stall mode: a slow or unreachable database — or simply a wait for a pooled connection
     * — holds the monitor for as long as the I/O takes, and every incoming view or download request
     * thread blocks behind it. The read-path guard added in #232 does not cover this; it only covers
     * reads.
     *
     * <p>So the critical section is now just a copy and a clear, which is bounded by the size of the
     * map. A caller recording a view during the write lands in the now-empty map and is picked up by
     * the next run, rather than waiting.
     *
     * <p>Counts are dropped from memory at drain time, before the write is attempted, so a failed
     * insert loses that hour's counts for the affected document. That is the existing behaviour —
     * {@link #syncDBHelper} already swallowed per-document failures and the map was cleared
     * regardless.
     *
     * <p>The sync is skipped outright, before anything is drained, while the schema has never been
     * created — the database has been unreachable since startup: the counts stay in memory,
     * {@code lastRun} stays where it is, and the next run writes them as one longer window. Draining
     * anyway would lose every count against a database already known to be down, at the price of a
     * connection timeout per document. It also matters for correctness on PostgreSQL:
     * {@code SimpleJdbcInsert} resolves its table once and keeps it, and with no {@code public.views} the
     * driver's metadata offers {@code information_schema.views} instead, so a view inserter first used
     * before the table exists stays bound to that system view until restart. Once the schema exists this
     * check is a volatile read, so an outage after that is handled as above, by losing that hour's
     * counts.
     */
    @Scheduled(initialDelay=TimeConstants.ONE_HOUR, fixedDelay=TimeConstants.ONE_HOUR)
    public void syncDB() {
        log.info("Exporting metric counts");
        if (!ensureSchema()) {
            log.warn("Metrics database unavailable, keeping counts in memory until the next sync");
            return;
        }
        val windowStart = lastRun;
        lastRun = Instant.now().getEpochSecond();
        syncTable(viewed, viewInserter, windowStart);
        syncTable(downloaded, downloadInserter, windowStart);
    }

    private void syncTable(Map<String, Set<String>> tableMap, SimpleJdbcInsert inserter, long windowStart) {
        Map<String, Integer> drained = new HashMap<>();
        synchronized (tableMap) {
            tableMap.forEach((doc, addrs) -> drained.put(doc, addrs.size()));
            tableMap.clear();
        }
        if (drained.isEmpty()) {
            return;
        }
        syncDBHelper(drained, inserter, windowStart);
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void updateDB() {
        log.info("Updating document titles and record types");
        ensureSchema();
        updateDBHelper(VIEW_TABLE);
        updateDBHelper(DOWNLOAD_TABLE);
    }

    private void recordMetric(@NonNull Map<String, Set<String>> map, @NonNull String uuid, @NonNull String addr) {
        map.computeIfAbsent(uuid, k -> new HashSet<>()).add(addr);
    }

    /**
     * Returns {@code null} rather than propagating, so a metrics outage costs a counter instead of the
     * whole record page. Not engine-specific: this covered {@code SQLITE_BUSY} when the database was
     * SQLite on a network share, and it covers an unreachable PostgreSQL, a connection-timeout on an
     * exhausted pool, or a failover now that the database is remote. A record page must not 500
     * because a metrics query failed, whatever the engine.
     */
    private @Nullable Integer totalAmount(@NonNull String table, @NonNull String uuid) {
        try {
            return jdbcTemplate.queryForObject(TOTAL_STATEMENT.formatted(table), Integer.class, uuid);
        } catch (DataAccessException ex) {
            log.warn("Could not read {} total for {}: {}", table, uuid, ex.getMessage());
            return null;
        }
    }

    private void updateDBHelper(String table) {
        List<String> distinctDocs = jdbcTemplate.queryForList(DISTINCT_DOCS_QUERY.formatted(table), String.class);
        distinctDocs.forEach((doc) -> {
            MetadataDocument document;
            try {
                log.debug("UPDATING title and type of document ID {} for {} table", doc, table);
                document = documentRepository.read(doc);
                jdbcTemplate.update(UPDATE_STATEMENT.formatted(table), document.getTitle(), document.getType(), doc);
            } catch (Exception e) {
                log.error("Error reading document from repository {}", doc, e);
            }
        });
    }

    /** Runs outside the monitor — see {@link #syncDB}. */
    private void syncDBHelper(Map<String, Integer> drained, SimpleJdbcInsert inserter, long windowStart) {
        drained.forEach((doc, amount) -> {
            MetadataDocument document;
            try {
                document = documentRepository.read(doc);
                inserter.execute(Map.of(
                    "start_timestamp", windowStart,
                    "end_timestamp", Instant.now().getEpochSecond(),
                    "amount", amount,
                    "document", doc,
                    "doc_title", document.getTitle(),
                    "record_type", document.getType()
                ));
            } catch (Exception e) {
                log.error("Error reading document from repository {}", doc, e);
            }
        });
    }

    /**
     * Parameters are collected as {@code Object}s and bound with {@code setObject}, so each keeps its
     * Java type. They used to be bound with {@code setString} throughout, which pgjdbc sends as
     * {@code varchar}; PostgreSQL has no {@code integer >= varchar} operator, so any date filter failed
     * with "operator does not exist" (SQLSTATE 42883). H2 and SQLite coerce the string silently, which is
     * why only a real PostgreSQL shows it — see {@code JDBCMetricsServicePostgresTest}.
     *
     * <p>Two more differences from SQLite are handled here. The document filter lower-cases both sides,
     * because SQLite's {@code LIKE} ignores ASCII case and PostgreSQL's does not. And the result is always
     * ordered, ending on {@code document}: the query always has a {@code LIMIT}, SQLite's grouping happens
     * to come out in key order, and PostgreSQL's does not, so without it the rows returned — and which
     * ones a tie at the limit keeps — would be arbitrary.</p>
     */
    public List<Map<String,String>> getMetricsReport(Instant startDate, Instant endDate, String orderBy, String ordering, List<String> recordType, String docId, Integer noOfRecords) {
        String sql = """
            SELECT t.document, coalesce(t.doc_title, '') AS doc_title, coalesce(t.record_type, '') AS record_type, sum(t.views) AS views, sum(t.downloads) AS downloads
            FROM (
                SELECT document, doc_title, record_type, amount AS downloads, 0 AS views FROM downloads WHERE %s
                UNION ALL
                SELECT document, doc_title, record_type, 0 AS downloads, amount AS views FROM views WHERE %s
            ) t
            GROUP BY document, doc_title, record_type
        """;

        ensureSchema();

        List<Object> whereVal = new ArrayList<>();
        StringBuilder where = new StringBuilder("1=1");
        if (startDate != null) {
            whereVal.add(startDate.getEpochSecond());
            where.append(" AND start_timestamp >= ?");
        }
        if (endDate != null) {
            whereVal.add(endDate.getEpochSecond());
            where.append(" AND end_timestamp <= ?");
        }
        if (docId != null && !docId.isBlank()) {
            whereVal.add("%" + docId + "%");
            where.append(" AND lower(document) LIKE lower(?)");
        }
        if (recordType != null && !recordType.isEmpty()) {
            where.append(" AND record_type IN (");
            for (String type : recordType) {
                whereVal.add(type);
                where.append("?,");
            }
            where.setCharAt(where.length() - 1, ')');
        }

        StringBuilder sqlBuilder = new StringBuilder(sql.formatted(where, where));
        sqlBuilder.append(" ORDER BY ");
        if (orderBy != null && !orderBy.isBlank()) {
            sqlBuilder.append(switch (orderBy) {
                case "views", "downloads" -> orderBy;
                default -> "document";
            });
            if (ordering != null && ordering.equals("descending")) {
                sqlBuilder.append(" DESC");
            }
            sqlBuilder.append(", ");
        }
        sqlBuilder.append("document");

        sqlBuilder.append(" LIMIT ");
        sqlBuilder.append(noOfRecords != null && noOfRecords >= 0 ? noOfRecords : 100);

        log.debug("Metrics report sql: {}", sqlBuilder);

        return jdbcTemplate.query(
            sqlBuilder.toString(), preparedStatement -> {
                int index = 1;
                int valSize = whereVal.size();
                for (Object val : whereVal) {
                    preparedStatement.setObject(index, val);
                    preparedStatement.setObject(valSize + index, val);
                    index++;
                }
            },
            new ReportMapper()
        );
    }

    static class ReportMapper implements RowMapper<Map<String, String>> {
        @Override
        public Map<String, String> mapRow(ResultSet rs, int map) throws SQLException {
            LinkedHashMap<String, String> row = new LinkedHashMap<>();
            row.put("document", rs.getString("document"));
            row.put("docTitle", rs.getString("doc_title"));
            row.put("recordType", rs.getString("record_type"));
            row.put("views", String.valueOf(rs.getInt("views")));
            row.put("downloads", String.valueOf(rs.getInt("downloads")));
            return row;
        }
    }
}
