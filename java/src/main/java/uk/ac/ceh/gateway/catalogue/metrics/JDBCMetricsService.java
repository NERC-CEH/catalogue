package uk.ac.ceh.gateway.catalogue.metrics;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
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
public class JDBCMetricsService implements MetricsService {
    @NonNull private final Map<String, Set<String>> viewed;
    @NonNull private final Map<String, Set<String>> downloaded;
    @NonNull private final SimpleJdbcInsert viewInserter;
    @NonNull private final SimpleJdbcInsert downloadInserter;
    @NonNull private final JdbcTemplate jdbcTemplate;
    private long lastRun;
    private final DocumentRepository documentRepository;

    // SQLite has no built-in datetime type, so we store dates as Unix timestamps (seconds since 1 Jan 1970)
    private static final String CREATE_STATEMENT = """
        CREATE TABLE IF NOT EXISTS %s (
            start_timestamp integer NOT NULL,
            end_timestamp integer NOT NULL,
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
     * <p>Creating this on an existing database is a one-off cost at startup, proportional to the table
     * size; the timing is logged. It is deliberately {@code IF NOT EXISTS} so restarts are free.</p>
     */
    private static final String INDEX_STATEMENT =
        "CREATE INDEX IF NOT EXISTS idx_%1$s_document_amount ON %1$s (document, amount)";
    private static final String TOTAL_STATEMENT = "SELECT coalesce(sum(amount), 0) FROM %s WHERE document = ?";
    private static final String UPDATE_STATEMENT = "UPDATE %s SET doc_title = ?, record_type = ? WHERE document = ?";
    private static final String DISTINCT_DOCS_QUERY = "SELECT DISTINCT document FROM %s";
    private static final String VIEW_TABLE = "views";
    private static final String DOWNLOAD_TABLE = "downloads";

    /**
     * Separate caches per count, not one shared cache: {@link #totalViews} and {@link #totalDownloads}
     * take the same single {@code String} argument, so under the default key generator a shared cache
     * would give them the same key and one would serve the other's number.
     */
    public static final String VIEW_TOTALS_CACHE = "metrics-view-totals";
    public static final String DOWNLOAD_TOTALS_CACHE = "metrics-download-totals";

    public JDBCMetricsService(@NonNull DataSource dataSource,
                              DocumentRepository documentRepository) {
        log.info("Creating");
        this.documentRepository = documentRepository;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.viewInserter = new SimpleJdbcInsert(dataSource).withTableName(VIEW_TABLE);
        this.downloadInserter = new SimpleJdbcInsert(dataSource).withTableName(DOWNLOAD_TABLE);
        this.viewed = Collections.synchronizedMap(new HashMap<>());
        this.downloaded = Collections.synchronizedMap(new HashMap<>());

        List.of(VIEW_TABLE, DOWNLOAD_TABLE).forEach(table -> {
            jdbcTemplate.execute(CREATE_STATEMENT.formatted(table));
            val startedAt = System.currentTimeMillis();
            jdbcTemplate.execute(INDEX_STATEMENT.formatted(table));
            log.info("Ensured index on {}(document, amount) in {}ms", table, System.currentTimeMillis() - startedAt);
        });
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

    @Scheduled(initialDelay=TimeConstants.ONE_HOUR, fixedDelay=TimeConstants.ONE_HOUR)
    public void syncDB() {
        log.info("Exporting metric counts");
        synchronized (viewed) {
            syncDBHelper(viewed, viewInserter);
            viewed.clear();
        }
        synchronized (downloaded) {
            syncDBHelper(downloaded, downloadInserter);
            downloaded.clear();
        }
        lastRun = Instant.now().getEpochSecond();
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void updateDB() {
        log.info("Updating document titles and record types");
        updateDBHelper(VIEW_TABLE);
        updateDBHelper(DOWNLOAD_TABLE);
    }

    private void recordMetric(@NonNull Map<String, Set<String>> map, @NonNull String uuid, @NonNull String addr) {
        map.computeIfAbsent(uuid, k -> new HashSet<>()).add(addr);
    }

    /**
     * Returns {@code null} rather than propagating, so a metrics outage costs a counter instead of the
     * whole record page. The database is SQLite on a network share, and a read that overlaps the hourly
     * sync's writes can still exhaust its busy timeout and fail with {@code SQLITE_BUSY}.
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

    private void syncDBHelper(Map<String, Set<String>> tableMap, SimpleJdbcInsert inserter) {
        tableMap.forEach((doc, viewers) -> {
            MetadataDocument document;
            try {
                document = documentRepository.read(doc);
                inserter.execute(Map.of(
                    "start_timestamp", lastRun,
                    "end_timestamp", Instant.now().getEpochSecond(),
                    "amount", viewers.size(),
                    "document", doc,
                    "doc_title", document.getTitle(),
                    "record_type", document.getType()
                ));
            } catch (Exception e) {
                log.error("Error reading document from repository {}", doc, e);
            }
        });
    }

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

        ArrayList<String> whereVal = new ArrayList<>();
        StringBuilder where = new StringBuilder("1=1");
        if (startDate != null) {
            String start = Long.toString(startDate.getEpochSecond());
            whereVal.add(start);
            where.append(" AND start_timestamp >= ?");
        }
        if (endDate != null) {
            String end = Long.toString(endDate.getEpochSecond());
            whereVal.add(end);
            where.append(" AND end_timestamp <= ?");
        }
        if (docId != null && !docId.isBlank()) {
            whereVal.add("%" + docId + "%");
            where.append(" AND document LIKE ?");
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
        if (orderBy != null && !orderBy.isBlank()) {
            sqlBuilder.append(" ORDER BY ");
            sqlBuilder.append(switch (orderBy) {
                case "views", "downloads" -> orderBy;
                default -> "document";
            });
            if (ordering != null && ordering.equals("descending")) {
                sqlBuilder.append(" DESC");
            }
        }

        sqlBuilder.append(" LIMIT ");
        sqlBuilder.append(noOfRecords != null && noOfRecords >= 0 ? noOfRecords : 100);

        log.info("Metrics report sql: {}", sqlBuilder);

        return jdbcTemplate.query(
            sqlBuilder.toString(), preparedStatement -> {
                int index = 1;
                int valSize = whereVal.size();
                for (String val : whereVal) {
                    preparedStatement.setString(index, val);
                    preparedStatement.setString(valSize + index, val);
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
