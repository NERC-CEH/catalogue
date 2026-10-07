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
    @NonNull private final JdbcTemplate jdbcTemplate;
    @NonNull private final MetricsSchemaMigrator schemaMigrator;
    private long lastRun;
    private final DocumentRepository documentRepository;

    /**
     * Set once the schema is known to be migrated. {@code volatile} so the fast path in
     * {@link #ensureSchema} needs no lock; the slow path takes {@link #schemaLock} so two callers racing
     * after a recovery do not both run the migration.
     */
    private volatile boolean schemaReady;
    private final Object schemaLock = new Object();

    /**
     * Every statement written out in full, per table, rather than a template with the table name
     * interpolated by {@code String.formatted}. The values were private constants so that was never
     * injectable, but it built the SQL text on every call; these are compile-time constants.
     *
     * <p>No DDL: the schema belongs to the Flyway migrations under {@code db/metrics/} (#235), applied
     * by {@link MetricsSchemaMigrator}. The totals query is served by the covering
     * {@code idx_<table>_document_amount} index those create, as #242's was on SQLite.</p>
     *
     * <p>The insert names its columns rather than going through {@code SimpleJdbcInsert}. That read the
     * table's columns from the driver's metadata, which (a) would include the new {@code id} and bind
     * it as {@code NULL}, and (b) on PostgreSQL, if first used before {@code public.views} existed,
     * resolved {@code views} to {@code information_schema.views} and stayed bound to it until restart.
     * Naming the columns also keeps the insert valid on the production SQLite file, whose tables predate
     * the {@code id} column.</p>
     */
    private record Statements(String insert, String total, String count, String delete, String update, String distinctDocs) { }

    private static final String VIEW_TABLE = "views";
    private static final String DOWNLOAD_TABLE = "downloads";

    private static final Statements VIEW_SQL = new Statements(
        "INSERT INTO views (start_timestamp, end_timestamp, amount, document, doc_title, record_type) VALUES (?, ?, ?, ?, ?, ?)",
        "SELECT coalesce(sum(amount), 0) FROM views WHERE document = ?",
        "SELECT count(*) FROM views WHERE document = ?",
        "DELETE FROM views WHERE document = ?",
        "UPDATE views SET doc_title = ?, record_type = ? WHERE document = ?",
        "SELECT DISTINCT document FROM views"
    );
    private static final Statements DOWNLOAD_SQL = new Statements(
        "INSERT INTO downloads (start_timestamp, end_timestamp, amount, document, doc_title, record_type) VALUES (?, ?, ?, ?, ?, ?)",
        "SELECT coalesce(sum(amount), 0) FROM downloads WHERE document = ?",
        "SELECT count(*) FROM downloads WHERE document = ?",
        "DELETE FROM downloads WHERE document = ?",
        "UPDATE downloads SET doc_title = ?, record_type = ? WHERE document = ?",
        "SELECT DISTINCT document FROM downloads"
    );

    /**
     * Both tables in one relation, so the report's filter is written — and bound — once. It used to be
     * substituted into each half of a {@code UNION ALL} and every parameter bound twice by index
     * arithmetic. A single-reference CTE is inlined on PostgreSQL 12+ (and SQLite), and the planner pushes
     * the {@code WHERE} down into both branches, so each table is still searched through its own indexes.
     */
    private static final String REPORT_SQL = """
        WITH metrics AS (
            SELECT document, doc_title, record_type, start_timestamp, end_timestamp, amount AS views, 0 AS downloads FROM views
            UNION ALL
            SELECT document, doc_title, record_type, start_timestamp, end_timestamp, 0 AS views, amount AS downloads FROM downloads
        )
        SELECT document, coalesce(doc_title, '') AS doc_title, coalesce(record_type, '') AS record_type, sum(views) AS views, sum(downloads) AS downloads
        FROM metrics
        WHERE %s
        GROUP BY document, doc_title, record_type
        """;

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
     * consumer that runs SQL while the context is starting. The schema is now migrated by
     * {@link MetricsSchemaMigrator}, from {@link #ensureSchema}.
     */
    public JDBCMetricsService(@NonNull DataSource dataSource,
                              DocumentRepository documentRepository,
                              @NonNull MetricsSchemaMigrator schemaMigrator) {
        log.info("Creating");
        this.documentRepository = documentRepository;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.schemaMigrator = schemaMigrator;
        this.viewed = Collections.synchronizedMap(new HashMap<>());
        this.downloaded = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * First attempt at the schema migration, once the context is up. {@code ApplicationReadyEvent} is
     * published before Spring Boot flips readiness to {@code ACCEPTING_TRAFFIC}, so on a healthy database
     * the tables and indexes exist before Kubernetes routes any traffic to the pod.
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
     * Runs the Flyway migrations if the schema is not already known to be current, and reports whether it
     * now is. Never throws: an unreachable database (or a failed migration) is logged and left for the
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
                val startedAt = System.currentTimeMillis();
                schemaMigrator.migrate();
                log.info("Metrics schema migrated in {}ms", System.currentTimeMillis() - startedAt);
                schemaReady = true;
            } catch (RuntimeException ex) {
                // RuntimeException, not DataAccessException: Flyway reports an unreachable database as a
                // FlywayException, which is outside Spring's hierarchy.
                log.warn("Metrics database schema could not be migrated, will retry on next use: {}", ex.getMessage());
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
        return totalAmount(VIEW_TABLE, VIEW_SQL, uuid);
    }

    /** Cached on the same terms as {@link #totalViews}, in its own cache to keep the keys apart. */
    @Override
    @Cacheable(cacheNames = DOWNLOAD_TOTALS_CACHE, unless = "#result == null")
    public @Nullable Integer totalDownloads(@NonNull String uuid) {
        return totalAmount(DOWNLOAD_TABLE, DOWNLOAD_SQL, uuid);
    }

    @Override
    public boolean hasMetricsFor(@NonNull String uuid) {
        ensureSchema();
        return count(VIEW_SQL, uuid) > 0 || count(DOWNLOAD_SQL, uuid) > 0;
    }

    /**
     * Deletes by {@code document} from both tables independently: a record can have views without
     * downloads or vice versa, and the caller only wants to know whether anything was removed, not from
     * which table.
     */
    @Override
    public boolean deleteMetricsFor(@NonNull String uuid) {
        ensureSchema();
        int viewsDeleted = jdbcTemplate.update(VIEW_SQL.delete(), uuid);
        int downloadsDeleted = jdbcTemplate.update(DOWNLOAD_SQL.delete(), uuid);
        return viewsDeleted > 0 || downloadsDeleted > 0;
    }

    private int count(Statements sql, String uuid) {
        Integer count = jdbcTemplate.queryForObject(sql.count(), Integer.class, uuid);
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
     * migrated — the database has been unreachable since startup: the counts stay in memory,
     * {@code lastRun} stays where it is, and the next run writes them as one longer window. Draining
     * anyway would lose every count against a database already known to be down, at the price of a
     * connection timeout per document. Once the schema exists this check is a volatile read, so an outage
     * after that is handled as above, by losing that hour's counts.
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
        syncTable(viewed, VIEW_SQL, windowStart);
        syncTable(downloaded, DOWNLOAD_SQL, windowStart);
    }

    private void syncTable(Map<String, Set<String>> tableMap, Statements sql, long windowStart) {
        Map<String, Integer> drained = new HashMap<>();
        synchronized (tableMap) {
            tableMap.forEach((doc, addrs) -> drained.put(doc, addrs.size()));
            tableMap.clear();
        }
        if (drained.isEmpty()) {
            return;
        }
        syncDBHelper(drained, sql, windowStart);
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void updateDB() {
        log.info("Updating document titles and record types");
        ensureSchema();
        updateDBHelper(VIEW_TABLE, VIEW_SQL);
        updateDBHelper(DOWNLOAD_TABLE, DOWNLOAD_SQL);
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
    private @Nullable Integer totalAmount(@NonNull String table, @NonNull Statements sql, @NonNull String uuid) {
        try {
            return jdbcTemplate.queryForObject(sql.total(), Integer.class, uuid);
        } catch (DataAccessException ex) {
            log.warn("Could not read {} total for {}: {}", table, uuid, ex.getMessage());
            return null;
        }
    }

    private void updateDBHelper(String table, Statements sql) {
        List<String> distinctDocs = jdbcTemplate.queryForList(sql.distinctDocs(), String.class);
        distinctDocs.forEach((doc) -> {
            MetadataDocument document;
            try {
                log.debug("UPDATING title and type of document ID {} for {} table", doc, table);
                document = documentRepository.read(doc);
                jdbcTemplate.update(sql.update(), document.getTitle(), document.getType(), doc);
            } catch (Exception e) {
                log.error("Error reading document from repository {}", doc, e);
            }
        });
    }

    /** Runs outside the monitor — see {@link #syncDB}. */
    private void syncDBHelper(Map<String, Integer> drained, Statements sql, long windowStart) {
        drained.forEach((doc, amount) -> {
            MetadataDocument document;
            try {
                document = documentRepository.read(doc);
                // Longs for the timestamps, so they bind as bigint, never narrowed to a 32-bit integer.
                jdbcTemplate.update(sql.insert(),
                    windowStart,
                    Instant.now().getEpochSecond(),
                    amount,
                    doc,
                    document.getTitle(),
                    document.getType());
            } catch (Exception e) {
                log.error("Error reading document from repository {}", doc, e);
            }
        });
    }

    /** The report's SQL and its parameters, in bind order — one value per placeholder. */
    record ReportQuery(String sql, List<Object> params) { }

    /**
     * Parameters are collected as {@code Object}s and bound with {@code setObject}, so each keeps its
     * Java type: the epoch-second filters go over as {@code Long} (bigint) and the record types and
     * document pattern as strings. They used to be bound with {@code setString} throughout, which pgjdbc
     * sends as {@code varchar}; PostgreSQL has no {@code bigint >= varchar} operator, so any date filter
     * failed with "operator does not exist" (SQLSTATE 42883). H2 and SQLite coerce the string silently,
     * which is why only a real PostgreSQL shows it — see {@code JDBCMetricsServicePostgresTest}.
     *
     * <p>Two more differences from SQLite are handled here. The document filter lower-cases both sides,
     * because SQLite's {@code LIKE} ignores ASCII case and PostgreSQL's does not. And the result is always
     * ordered, ending on {@code document}: the query always has a {@code LIMIT}, SQLite's grouping happens
     * to come out in key order, and PostgreSQL's does not, so without it the rows returned — and which
     * ones a tie at the limit keeps — would be arbitrary.</p>
     *
     * <p>The date and record-type filters are served by the {@code idx_<table>_start_end} and
     * {@code idx_<table>_record_type_start} indexes on PostgreSQL. The document filter is a leading-wildcard
     * {@code LIKE '%...%'}, which no index can serve; it needs redesigning, not indexing (#243).</p>
     */
    public List<Map<String,String>> getMetricsReport(Instant startDate, Instant endDate, String orderBy, String ordering, List<String> recordType, String docId, Integer noOfRecords) {
        ensureSchema();

        val query = reportQuery(startDate, endDate, orderBy, ordering, recordType, docId, noOfRecords);
        log.info("Metrics report sql: {}", query.sql());

        return jdbcTemplate.query(
            query.sql(), preparedStatement -> {
                int index = 1;
                for (Object val : query.params()) {
                    preparedStatement.setObject(index++, val);
                }
            },
            new ReportMapper()
        );
    }

    /** Builds the report query without running it. Package-private for the {@code EXPLAIN} tests. */
    static ReportQuery reportQuery(Instant startDate, Instant endDate, String orderBy, String ordering, List<String> recordType, String docId, Integer noOfRecords) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("1=1");
        if (startDate != null) {
            params.add(startDate.getEpochSecond());
            where.append(" AND start_timestamp >= ?");
        }
        if (endDate != null) {
            params.add(endDate.getEpochSecond());
            where.append(" AND end_timestamp <= ?");
        }
        if (docId != null && !docId.isBlank()) {
            params.add("%" + docId + "%");
            where.append(" AND lower(document) LIKE lower(?)");
        }
        if (recordType != null && !recordType.isEmpty()) {
            where.append(" AND record_type IN (");
            for (String type : recordType) {
                params.add(type);
                where.append("?,");
            }
            where.setCharAt(where.length() - 1, ')');
        }

        StringBuilder sqlBuilder = new StringBuilder(REPORT_SQL.formatted(where));
        sqlBuilder.append("ORDER BY ");
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

        return new ReportQuery(sqlBuilder.toString(), Collections.unmodifiableList(params));
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
