package uk.ac.ceh.gateway.catalogue.metrics;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Counts views and downloads in memory and writes them hourly to the {@code metrics} schema of the
 * PostgreSQL {@code catalogue} database ({@code metrics.database.engine=postgres}).
 */
@Profile("metrics")
@ConditionalOnProperty(name = "metrics.database.engine", havingValue = "postgres")
@Slf4j
@Service
public class JDBCMetricsService implements MetricsService, ApplicationListener<ApplicationReadyEvent> {
    @NonNull private final Map<String, Set<String>> viewed;
    @NonNull private final Map<String, Set<String>> downloaded;
    @NonNull private final JdbcTemplate jdbcTemplate;
    @NonNull private final Flyway flyway;
    private volatile long lastRun;
    private final Clock clock;
    private final DocumentRepository documentRepository;

    private volatile boolean schemaReady;
    private final Object schemaLock = new Object();

    private final ReentrantLock syncLock = new ReentrantLock();
    private volatile long shutdownFlushTimeoutMillis;

    public static final String SCHEMA = "metrics";
    public static final String MIGRATIONS = "classpath:db/metrics";

    private static final java.util.regex.Pattern UUID_PATTERN =
        java.util.regex.Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private static final String UPSERT_DOCUMENT = """
        INSERT INTO metrics.documents (document_id, doc_title, record_type)
        VALUES (?, ?, ?)
        ON CONFLICT (document_id) DO UPDATE SET
            doc_title   = coalesce(EXCLUDED.doc_title, documents.doc_title),
            record_type = coalesce(EXCLUDED.record_type, documents.record_type),
            removed_at  = CASE WHEN EXCLUDED.doc_title IS NULL THEN documents.removed_at END
        """;
    private static final String INSERT_COUNT = """
        INSERT INTO metrics.metric_counts (document_id, metric_type, counted_from, counted_until, amount)
        VALUES (?, ?, ?, ?, ?)
        """;
    static final String TOTAL_STATEMENT =
        "SELECT coalesce(sum(amount), 0) FROM metrics.metric_counts WHERE document_id = ? AND metric_type = ?";
    private static final String HAS_METRICS_STATEMENT =
        "SELECT EXISTS (SELECT 1 FROM metrics.metric_counts WHERE document_id = ?)";
    private static final String DELETE_COUNTS = "DELETE FROM metrics.metric_counts WHERE document_id = ?";
    private static final String DELETE_DOCUMENT = "DELETE FROM metrics.documents WHERE document_id = ?";
    private static final String DOCUMENTS_QUERY =
        "SELECT document_id::text FROM metrics.documents WHERE removed_at IS NULL ORDER BY document_id";
    private static final String UPDATE_STATEMENT =
        "UPDATE metrics.documents SET doc_title = ?, record_type = ? WHERE document_id = ? AND (doc_title, record_type) IS DISTINCT FROM (?, ?)";
    private static final String VIEW = "view";
    private static final String DOWNLOAD = "download";

    /**
     * Separate caches per count, not one shared cache: {@link #totalViews} and {@link #totalDownloads}
     * take the same single {@code String} argument, so under the default key generator a shared cache
     * would give them the same key and one would serve the other's number.
     */
    public static final String VIEW_TOTALS_CACHE = "metrics-view-totals";
    public static final String DOWNLOAD_TOTALS_CACHE = "metrics-download-totals";

    @Autowired
    public JDBCMetricsService(@NonNull DataSource dataSource,
                              DocumentRepository documentRepository) {
        this(dataSource, documentRepository, Clock.systemUTC());
    }

    /** For tests, which need to control the window timestamps. */
    JDBCMetricsService(@NonNull DataSource dataSource,
                       DocumentRepository documentRepository,
                       @NonNull Clock clock) {
        log.info("Creating");
        this.clock = clock;
        this.lastRun = clock.instant().getEpochSecond();
        this.documentRepository = documentRepository;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.flyway = flyway(dataSource).load();
        this.viewed = Collections.synchronizedMap(new HashMap<>());
        this.downloaded = Collections.synchronizedMap(new HashMap<>());
    }

    static FluentConfiguration flyway(DataSource dataSource) {
        return Flyway.configure()
            .dataSource(dataSource)
            .schemas(SCHEMA)
            .defaultSchema(SCHEMA)
            .createSchemas(true)
            .locations(MIGRATIONS)
            .failOnMissingLocations(true);
    }

    @Autowired
    void setShutdownFlushTimeoutMillis(@Value("${metrics.shutdown-flush.timeout-millis}") long millis) {
        if (millis <= 0) {
            throw new IllegalArgumentException("metrics.shutdown-flush.timeout-millis must be positive, was " + millis);
        }
        this.shutdownFlushTimeoutMillis = millis;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        initialiseSchemaOnStartup();
    }

    public void initialiseSchemaOnStartup() {
        ensureSchema();
    }

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
                val result = flyway.migrate();
                log.info("Metrics schema migrated: {} migration(s) applied, now at version {} ({}ms)",
                    result.migrationsExecuted, result.targetSchemaVersion, System.currentTimeMillis() - startedAt);
                schemaReady = true;
            } catch (FlywayException ex) {
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
     * Cached because this is called while a record page renders, so an uncached read puts a database
     * query directly on the render critical path.
     */
    @Override
    @Cacheable(cacheNames = VIEW_TOTALS_CACHE, unless = "#result == null")
    public @Nullable Integer totalViews(@NonNull String uuid) {
        return totalAmount(VIEW, uuid);
    }

    @Override
    @Cacheable(cacheNames = DOWNLOAD_TOTALS_CACHE, unless = "#result == null")
    public @Nullable Integer totalDownloads(@NonNull String uuid) {
        return totalAmount(DOWNLOAD, uuid);
    }

    @Override
    public boolean hasMetricsFor(@NonNull String uuid) {
        ensureSchema();
        return uuid(uuid)
            .map(id -> Boolean.TRUE.equals(jdbcTemplate.queryForObject(HAS_METRICS_STATEMENT, Boolean.class, id)))
            .orElse(false);
    }

    /**
     * Deletes every view and download of {@code uuid}, and its {@code documents} row, which nothing
     * references once its counts are gone.
     */
    @Override
    public boolean deleteMetricsFor(@NonNull String uuid) {
        ensureSchema();
        val id = uuid(uuid);
        if (id.isEmpty()) {
            return false;
        }
        int deleted = jdbcTemplate.update(DELETE_COUNTS, id.get());
        jdbcTemplate.update(DELETE_DOCUMENT, id.get());
        return deleted > 0;
    }

    @Scheduled(
        initialDelayString = "${metrics.sync.interval-millis}",
        fixedDelayString = "${metrics.sync.interval-millis}"
    )
    public void syncDB() {
        syncLock.lock();
        try {
            writePendingCounts(Deadline.NONE);
        } finally {
            syncLock.unlock();
        }
    }

    /**
     * Flush the accumulated metrics count(s) from memory to database during application shutdown
     */
    @PreDestroy
    public void flushOnShutdown() {
        val pendingViews = viewed.size();
        val pendingDownloads = downloaded.size();
        if (pendingViews == 0 && pendingDownloads == 0) {
            log.info("Shutdown: no metric counts pending since the last sync, nothing to flush");
            return;
        }
        val budgetMillis = shutdownFlushTimeoutMillis;
        val deadline = Deadline.after(budgetMillis);
        val startedAt = System.nanoTime();
        log.info("Shutdown: flushing metric counts for {} viewed and {} downloaded document(s) (budget {}ms)",
            pendingViews, pendingDownloads, budgetMillis);

        boolean locked;
        try {
            locked = syncLock.tryLock(deadline.remainingNanos(), TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            locked = false;
        }
        if (!locked) {
            log.warn("Shutdown: the scheduled metrics sync was still running after {}ms; counts for {} viewed "
                + "and {} downloaded document(s) were not flushed and are lost",
                budgetMillis, viewed.size(), downloaded.size());
            return;
        }
        try {
            val complete = writePendingCounts(deadline);
            val tookMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            if (complete) {
                log.info("Shutdown: metric counts flushed in {}ms", tookMillis);
            } else {
                log.warn("Shutdown: metric counts flush incomplete after {}ms, see the warnings above for "
                    + "what was not written", tookMillis);
            }
        } catch (RuntimeException e) {
            log.error("Shutdown: metric counts flush failed", e);
        } finally {
            syncLock.unlock();
        }
    }

    private boolean writePendingCounts(Deadline deadline) {
        log.info("Exporting metric counts");
        if (!ensureSchema()) {
            log.warn("Metrics database unavailable, keeping counts in memory until the next sync");
            return false;
        }
        val windowStart = lastRun;
        val windowEnd = clock.instant().getEpochSecond();
        lastRun = windowEnd;
        val viewsComplete = syncTable(VIEW, viewed, windowStart, windowEnd, deadline);
        val downloadsComplete = syncTable(DOWNLOAD, downloaded, windowStart, windowEnd, deadline);
        return viewsComplete && downloadsComplete;
    }

    private boolean syncTable(String metricType, Map<String, Set<String>> tableMap,
                              long windowStart, long windowEnd, Deadline deadline) {
        Map<String, Integer> drained = new HashMap<>();
        synchronized (tableMap) {
            tableMap.forEach((doc, addrs) -> drained.put(doc, addrs.size()));
            tableMap.clear();
        }
        if (drained.isEmpty()) {
            return true;
        }
        return syncDBHelper(metricType, drained, windowStart, windowEnd, deadline);
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void updateDB() {
        log.info("Updating document titles and record types");
        if (!ensureSchema()) {
            return;
        }
        jdbcTemplate.queryForList(DOCUMENTS_QUERY, String.class).forEach(doc -> {
            try {
                log.debug("UPDATING title and type of document ID {}", doc);
                MetadataDocument document = documentRepository.read(doc);
                jdbcTemplate.update(UPDATE_STATEMENT, document.getTitle(), document.getType(), UUID.fromString(doc), document.getTitle(), document.getType());
            } catch (Exception e) {
                log.error("Error reading document from repository {}", doc, e);
            }
        });
    }

    private void recordMetric(@NonNull Map<String, Set<String>> map, @NonNull String uuid, @NonNull String addr) {
        map.computeIfAbsent(uuid, k -> new HashSet<>()).add(addr);
    }

    private @Nullable Integer totalAmount(@NonNull String metricType, @NonNull String uuid) {
        val id = uuid(uuid);
        if (id.isEmpty()) {
            return 0;
        }
        try {
            return jdbcTemplate.queryForObject(TOTAL_STATEMENT, Integer.class, id.get(), metricType);
        } catch (DataAccessException ex) {
            log.warn("Could not read {} total for {}: {}", metricType, uuid, ex.getMessage());
            return null;
        }
    }

    private boolean syncDBHelper(String metricType, Map<String, Integer> drained,
                                 long windowStart, long windowEnd, Deadline deadline) {
        int skippedDocuments = 0;
        int skippedAmount = 0;
        for (val entry : drained.entrySet()) {
            val doc = entry.getKey();
            val amount = entry.getValue();
            if (deadline.hasPassed()) {
                skippedDocuments++;
                skippedAmount += amount;
                continue;
            }
            val id = uuid(doc);
            if (id.isEmpty()) {
                log.warn("Not writing {} {}(s) for {}: not a UUID", amount, metricType, doc);
                continue;
            }
            String title = null;
            String recordType = null;
            try {
                MetadataDocument document = documentRepository.read(doc);
                title = document.getTitle();
                recordType = document.getType();
            } catch (Exception e) {
                log.error("Error reading document from repository {}", doc, e);
            }
            try {
                insertCount(id.get(), metricType, amount, windowStart, windowEnd, title, recordType);
            } catch (DataAccessException e) {
                log.error("Error writing {} {}(s) for {}", amount, metricType, doc, e);
            }
        }
        if (skippedDocuments > 0) {
            log.warn("Metrics flush ran out of time: {} {}(s) across {} of {} document(s) were not written",
                skippedAmount, metricType, skippedDocuments, drained.size());
            return false;
        }
        return true;
    }

    private void insertCount(UUID id, String metricType, int amount, long windowStart, long windowEnd,
                             @Nullable String title, @Nullable String recordType) {
        jdbcTemplate.execute((java.sql.Connection connection) -> {
            val autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (val upsert = connection.prepareStatement(UPSERT_DOCUMENT);
                 val insert = connection.prepareStatement(INSERT_COUNT)) {
                upsert.setObject(1, id);
                upsert.setString(2, title);
                upsert.setString(3, recordType);
                upsert.executeUpdate();
                insert.setObject(1, id);
                insert.setString(2, metricType);
                insert.setObject(3, at(windowStart));
                insert.setObject(4, at(windowEnd));
                insert.setInt(5, amount);
                insert.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
            return null;
        });
    }

    private static OffsetDateTime at(long epochSecond) {
        return Instant.ofEpochSecond(epochSecond).atOffset(ZoneOffset.UTC);
    }

    private static Optional<UUID> uuid(String document) {
        return UUID_PATTERN.matcher(document).matches() ? Optional.of(UUID.fromString(document)) : Optional.empty();
    }

    /**
     * Check if the preset time {@code atNanos} has passed
     */
    private record Deadline(long atNanos, boolean bounded) {
        static final Deadline NONE = new Deadline(0, false);

        static Deadline after(long millis) {
            return new Deadline(System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(millis), true);
        }

        boolean hasPassed() {
            return bounded && System.nanoTime() - atNanos >= 0;
        }

        long remainingNanos() {
            return bounded ? Math.max(0, atNanos - System.nanoTime()) : Long.MAX_VALUE;
        }
    }

    public List<Map<String,String>> getMetricsReport(Instant startDate, Instant endDate, String orderBy, String ordering, List<String> recordType, String docId, Integer noOfRecords) {
        ensureSchema();

        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("TRUE");
        if (startDate != null) {
            where.append(" AND m.counted_from >= ?");
            params.add(startDate.atOffset(ZoneOffset.UTC));
        }
        if (endDate != null) {
            where.append(" AND m.counted_until <= ? AND m.counted_from <= ?");
            params.add(endDate.atOffset(ZoneOffset.UTC));
            params.add(endDate.atOffset(ZoneOffset.UTC));
        }
        if (recordType != null && !recordType.isEmpty()) {
            where.append(" AND d.record_type IN (").append(String.join(",", Collections.nCopies(recordType.size(), "?"))).append(")");
            params.addAll(recordType);
        }
        if (docId != null && !docId.isBlank()) {
            where.append(" AND d.document_id::text ILIKE ?");
            params.add("%" + docId.strip().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
        }

        String order = "document";
        if (orderBy != null && !orderBy.isBlank()) {
            String column = switch (orderBy) {
                case "views", "downloads" -> orderBy;
                default -> "document";
            };
            order = column + ("descending".equals(ordering) ? " DESC" : "") + ", document";
        }
        params.add(noOfRecords != null && noOfRecords >= 0 ? noOfRecords : 100);

        String sql = """
            SELECT d.document_id::text AS document,
                   coalesce(d.doc_title, '') AS doc_title,
                   coalesce(d.record_type, '') AS record_type,
                   coalesce(sum(m.amount) FILTER (WHERE m.metric_type = 'view'), 0) AS views,
                   coalesce(sum(m.amount) FILTER (WHERE m.metric_type = 'download'), 0) AS downloads
            FROM metrics.metric_counts m
            JOIN metrics.documents d ON d.document_id = m.document_id
            WHERE %s
            GROUP BY d.document_id, d.doc_title, d.record_type
            ORDER BY %s
            LIMIT ?
            """.formatted(where, order);
        log.debug("Metrics report sql: {}", sql);

        return jdbcTemplate.query(sql, new ReportMapper(), params.toArray());
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
