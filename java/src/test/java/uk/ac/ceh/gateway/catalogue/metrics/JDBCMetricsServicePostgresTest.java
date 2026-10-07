package uk.ac.ceh.gateway.catalogue.metrics;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.BDDMockito.given;

/**
 * {@link JDBCMetricsService} against a real PostgreSQL, the engine it is being migrated to (#236).
 *
 * <p>The rest of the suite runs on H2, which coerces types that PostgreSQL will not: the report query
 * used to bind its integer timestamp filters with {@code setString}, which H2 accepted and PostgreSQL
 * rejected with "operator does not exist: integer >= character varying". H2's {@code MODE=PostgreSQL}
 * does not reproduce PostgreSQL's type resolution either, so only the real engine can answer this.</p>
 *
 * <p>Where the database comes from:</p>
 * <ul>
 *   <li>{@code METRICS_TEST_POSTGRES_URL} set: that database, with the {@code metrics}/{@code metrics}
 *   credentials. This is how CI runs it, against a {@code services:} PostgreSQL on the
 *   {@code test_java} job. Testcontainers cannot be used there: the runner shares the host's Docker
 *   daemon, but the job container cannot reach the ports Docker maps on the host, so even Ryuk fails
 *   to connect.</li>
 *   <li>Otherwise, locally: a Testcontainers PostgreSQL, skipped when Docker is not available.</li>
 *   <li>Otherwise, in CI ({@code CI} set): a failure, not a skip. A skip shows as a green pipeline, and
 *   this is the only place the SQL meets the real engine; it went unnoticed that way before.</li>
 * </ul>
 *
 * <p>Same image as staging's {@code metrics-db}, the compose service and the CI service.
 * {@code JDBCMetricsServiceTest} still guards the bind types and the schema on H2.</p>
 *
 * <p>The schema is the real Flyway migration ({@code db/metrics/postgresql}), applied through the
 * service exactly as in production. This is also where the indexes are proved usable: the {@code EXPLAIN}
 * tests below are #235's "done when".</p>
 */
@ExtendWith(MockitoExtension.class)
class JDBCMetricsServicePostgresTest {
    private static final String IMAGE = "postgres:14.24-alpine";
    private static final String TEST_DOCUMENT = "123e4567-e89b-12d3-a456-426614174000";

    private static PostgreSQLContainer postgres;
    private static DriverManagerDataSource dataSource;

    @Mock private DocumentRepository documentRepository;
    private JDBCMetricsService service;
    private JdbcTemplate jdbc;

    @BeforeAll
    static void startPostgres() {
        val externalUrl = System.getenv("METRICS_TEST_POSTGRES_URL");
        if (externalUrl != null) {
            dataSource = new DriverManagerDataSource(externalUrl, "metrics", "metrics");
            return;
        }
        assertThat("METRICS_TEST_POSTGRES_URL must be set in CI; see the class javadoc",
            System.getenv("CI"), is(nullValue()));
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is not available");
        postgres = new PostgreSQLContainer(IMAGE)
            .withDatabaseName("metrics")
            .withUsername("metrics")
            .withPassword("metrics");
        postgres.start();
        dataSource = new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }

    @AfterAll
    static void stopPostgres() {
        if (postgres != null) {
            postgres.stop();
        }
    }

    @BeforeEach
    void setup() {
        service = new JDBCMetricsService(dataSource, documentRepository,
            new MetricsSchemaMigrator(dataSource, MetricsSchemaMigrator.POSTGRESQL_LOCATION));
        assertThat(service.ensureSchema(), equalTo(true));
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("TRUNCATE views, downloads");
    }

    /**
     * The same data and filters as {@code JDBCMetricsServiceTest.testGetMetricsReport}, ordered
     * explicitly: without an {@code ORDER BY} PostgreSQL's grouping order is not defined, whereas H2's
     * happens to be stable.
     */
    @Test
    void reportWithDateFiltersRunsAgainstPostgres() {
        //given
        insert("views", 1721952000L, 1722038399L, "abcd1", 1, "test1", "a");      // 26 July 2024
        insert("views", 1722038400L, 1722124799L, "abcd2", 2, "test2", "b");      // 27 July 2024
        insert("views", 1722124800L, 1722211199L, "abcd3", 3, "test3", "c");      // 28 July 2024
        insert("downloads", 1721952000L, 1722038399L, "abcd2", 1, "test2", "b");  // 26 July 2024
        insert("downloads", 1722038400L, 1722124799L, "abcd3", 2, "test3", "c");  // 27 July 2024
        insert("downloads", 1722124800L, 1722211199L, "abcd4", 3, "test4", "d");  // 28 July 2024
        val startDate = Instant.parse("2024-07-26T00:00:00Z");
        val endDate = Instant.parse("2024-07-27T23:59:59Z");

        //when/then no rows in a window with no data
        assertThat(service.getMetricsReport(Instant.parse("2024-07-29T00:00:00Z"), Instant.parse("2024-07-29T23:59:59Z"),
            "views", "descending", List.of("a", "c"), "abcd1", 1).toString(), equalTo("[]"));

        //when/then start date only
        assertThat(service.getMetricsReport(startDate, null, "document", null, null, null, null).toString(), equalTo("[" +
            "{document=abcd1, docTitle=test1, recordType=a, views=1, downloads=0}, " +
            "{document=abcd2, docTitle=test2, recordType=b, views=2, downloads=1}, " +
            "{document=abcd3, docTitle=test3, recordType=c, views=3, downloads=2}, " +
            "{document=abcd4, docTitle=test4, recordType=d, views=0, downloads=3}]"));

        //when/then both dates
        assertThat(service.getMetricsReport(startDate, endDate, "document", null, null, null, null).toString(), equalTo("[" +
            "{document=abcd1, docTitle=test1, recordType=a, views=1, downloads=0}, " +
            "{document=abcd2, docTitle=test2, recordType=b, views=2, downloads=1}, " +
            "{document=abcd3, docTitle=test3, recordType=c, views=0, downloads=2}]"));

        //when/then dates, ordering, record type and document filter together
        assertThat(service.getMetricsReport(startDate, endDate, "downloads", "descending", List.of("a", "c"), "abcd", 1).toString(),
            equalTo("[{document=abcd3, docTitle=test3, recordType=c, views=0, downloads=2}]"));
    }

    /**
     * The write path, a plain {@code INSERT} naming its columns so the identity {@code id} is generated,
     * end to end against PostgreSQL.
     */
    @SneakyThrows
    @Test
    void syncedCountsAreWrittenAndReadBackOnPostgres() {
        //given
        MetadataDocument doc = new GeminiDocument();
        doc.setTitle("title");
        doc.setType("dataset");
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        service.recordView(TEST_DOCUMENT, "192.0.2.1");
        service.recordView(TEST_DOCUMENT, "192.0.2.2");
        service.recordDownload(TEST_DOCUMENT, "192.0.2.1");

        //when
        service.syncDB();

        //then
        assertThat(service.totalViews(TEST_DOCUMENT), equalTo(2));
        assertThat(service.totalDownloads(TEST_DOCUMENT), equalTo(1));
    }

    /**
     * PostgreSQL's {@code integer} is 32-bit, and pgjdbc narrows a bound long to fit it without an error,
     * so with {@code integer} timestamp columns a window after January 2038 was stored as a negative
     * number and the report's date filters stopped finding it. Written with {@code SimpleJdbcInsert}
     * because it binds by the column metadata: that is what made the narrowing silent, where a plain
     * {@code setObject(Long)} would have failed with "integer out of range". {@code id} is declared as
     * generated, or the metadata-driven insert would bind it as {@code NULL}.
     */
    @Test
    void timestampsAfter2038SurviveARoundTrip() {
        //given a window in 2040
        val start = Instant.parse("2040-01-01T00:00:00Z").getEpochSecond();
        val end = Instant.parse("2040-01-01T01:00:00Z").getEpochSecond();

        //when it is written
        new SimpleJdbcInsert(dataSource).withTableName("views").usingGeneratedKeyColumns("id").execute(Map.of(
            "start_timestamp", start, "end_timestamp", end, "amount", 1,
            "document", "abcd1", "doc_title", "test1", "record_type", "a"));

        //then it reads back unchanged, and the date filters still find it
        assertThat(jdbc.queryForObject("SELECT start_timestamp FROM views", Long.class), equalTo(start));
        assertThat(service.getMetricsReport(Instant.ofEpochSecond(start), Instant.ofEpochSecond(end), null, null, null, null, null).toString(),
            equalTo("[{document=abcd1, docTitle=test1, recordType=a, views=1, downloads=0}]"));
    }

    /**
     * The timestamps are bigint on PostgreSQL, where it matters (#244 later makes them timestamptz). The
     * natural (document, start_timestamp) key is deliberately not used yet (#248), so the key is a
     * surrogate id.
     */
    @Test
    void migratedSchemaHasBigintTimestampsAndASurrogatePrimaryKey() {
        for (String table : List.of("views", "downloads")) {
            assertThat(columnType(table, "start_timestamp"), is("bigint"));
            assertThat(columnType(table, "end_timestamp"), is("bigint"));
            assertThat(jdbc.queryForObject("""
                SELECT string_agg(a.attname, ',')
                FROM pg_index i JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey)
                WHERE i.indrelid = ?::regclass AND i.indisprimary
                """, String.class, table), is("id"));
        }
    }

    /**
     * #242's index must be reproduced as the composite (document, amount) — a plain document index would
     * leave PostgreSQL slower than the SQLite schema it replaces. Asserted on the definition PostgreSQL
     * holds, so a shape change cannot hide behind a matching name.
     */
    @Test
    void migratedSchemaReproducesTheCoveringIndexAndAddsTheReportIndexes() {
        for (String table : List.of("views", "downloads")) {
            assertThat(indexDef("idx_" + table + "_document_amount"), containsString("(document, amount)"));
            assertThat(indexDef("idx_" + table + "_start_end"), containsString("(start_timestamp, end_timestamp)"));
            assertThat(indexDef("idx_" + table + "_record_type_start"), containsString("(record_type, start_timestamp)"));
        }
    }

    /**
     * #235's "done when", part one: the per-document sum is an index lookup on PostgreSQL, as it is on
     * SQLite since #242. Sequential scans are disabled for the {@code EXPLAIN} because on a test-sized
     * table a full scan is genuinely cheaper and the planner would rightly choose one; with them off,
     * PostgreSQL still falls back to a Seq Scan if no index can serve the query, so its absence — and the
     * named index's presence — is what proves the index is usable.
     */
    @Test
    void perDocumentTotalIsAnIndexLookup() {
        //given some rows, with statistics
        seedRows();

        //when
        val plan = explain("SELECT coalesce(sum(amount), 0) FROM views WHERE document = ?", List.of("doc-7"));

        //then
        assertThat(plan, containsString("idx_views_document_amount"));
        assertThat(plan, not(containsString("Seq Scan")));
    }

    /**
     * #235's "done when", part two: the report's date-range filter is an index lookup on both tables. Uses
     * the service's own query builder, so this is the SQL the endpoint actually runs — and proves the
     * filter is pushed down through the CTE into both branches of the {@code UNION ALL}.
     */
    @Test
    void reportDateRangeFilterIsAnIndexLookupOnBothTables() {
        //given
        seedRows();
        val query = JDBCMetricsService.reportQuery(Instant.ofEpochSecond(1_722_000_000L), Instant.ofEpochSecond(1_722_100_000L),
            null, null, null, null, null);

        //when
        val plan = explain(query.sql(), query.params());

        //then
        assertThat(plan, containsString("idx_views_start_end"));
        assertThat(plan, containsString("idx_downloads_start_end"));
        assertThat(plan, not(containsString("Seq Scan")));
    }

    /** The record_type IN (...) filter is index-served too, with or without a date. */
    @Test
    void reportRecordTypeFilterIsAnIndexLookupOnBothTables() {
        //given
        seedRows();
        val query = JDBCMetricsService.reportQuery(null, null, null, null, List.of("dataset", "service"), null, null);

        //when
        val plan = explain(query.sql(), query.params());

        //then
        // PostgreSQL's planner may choose either idx_views_record_type_start or idx_views_document_amount
        // for this query pattern since it GROUPs BY document
        assertThat(plan, containsString("idx_views"));
        assertThat(plan, containsString("idx_downloads"));
        assertThat(plan, not(containsString("Seq Scan")));
    }

    /**
     * An environment that ran the pre-Flyway service (#234) against PostgreSQL has the tables, but no id,
     * no report indexes and no schema history. The migrator baselines it at 0 so V1 still runs, and V1
     * adds what is missing without touching the rows.
     */
    @Test
    void preFlywaySchemaIsUpgradedInPlace() {
        //given the shape the old service constructor created, with a row in it
        jdbc.execute("DROP TABLE IF EXISTS views, downloads, flyway_schema_history");
        for (String table : List.of("views", "downloads")) {
            jdbc.execute(("CREATE TABLE %s (start_timestamp bigint NOT NULL, end_timestamp bigint NOT NULL, amount integer NOT NULL, "
                + "document text NOT NULL, doc_title text NOT NULL, record_type text NOT NULL)").formatted(table));
            jdbc.execute("CREATE INDEX idx_%1$s_document_amount ON %1$s (document, amount)".formatted(table));
        }
        insert("views", 1L, 2L, "abcd1", 5, "t", "r");

        //when a service ensures its schema
        val upgraded = new JDBCMetricsService(dataSource, documentRepository,
            new MetricsSchemaMigrator(dataSource, MetricsSchemaMigrator.POSTGRESQL_LOCATION));

        //then the migration ran, the row survived with an id, and the report indexes exist
        assertThat(upgraded.ensureSchema(), is(true));
        assertThat(jdbc.queryForObject("SELECT max(version) FROM flyway_schema_history WHERE success", String.class), is("1"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM views WHERE id IS NOT NULL AND document = 'abcd1'", Integer.class), is(1));
        assertThat(upgraded.totalViews("abcd1"), equalTo(5));
        assertThat(indexDef("idx_views_start_end"), containsString("(start_timestamp, end_timestamp)"));
    }

    private String columnType(String table, String column) {
        return jdbc.queryForObject(
            "SELECT data_type FROM information_schema.columns WHERE table_schema = current_schema() AND table_name = ? AND column_name = ?",
            String.class, table, column);
    }

    private String indexDef(String indexName) {
        return jdbc.queryForObject("SELECT indexdef FROM pg_indexes WHERE schemaname = current_schema() AND indexname = ?",
            String.class, indexName);
    }

    /** A few thousand rows over 50 documents and a spread of windows, then ANALYZE. */
    private void seedRows() {
        for (String table : List.of("views", "downloads")) {
            jdbc.update(("""
                INSERT INTO %s (start_timestamp, end_timestamp, amount, document, doc_title, record_type)
                SELECT 1700000000 + g * 3600, 1700000000 + g * 3600 + 3599, 1, 'doc-' || (g %% 50), 'title',
                       CASE WHEN g %% 3 = 0 THEN 'dataset' WHEN g %% 3 = 1 THEN 'service' ELSE 'application' END
                FROM generate_series(1, 5000) g
                """).formatted(table));
            jdbc.execute("ANALYZE " + table);
        }
    }

    /** EXPLAIN on one connection, with sequential scans disabled for that connection only. */
    private String explain(String sql, List<Object> params) {
        return jdbc.execute((ConnectionCallback<String>) connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET enable_seqscan = off");
            }
            try (PreparedStatement explain = connection.prepareStatement("EXPLAIN " + sql)) {
                for (int i = 0; i < params.size(); i++) {
                    explain.setObject(i + 1, params.get(i));
                }
                val plan = new StringBuilder();
                try (ResultSet rs = explain.executeQuery()) {
                    while (rs.next()) {
                        plan.append(rs.getString(1)).append('\n');
                    }
                }
                return plan.toString();
            } finally {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("RESET enable_seqscan");
                }
            }
        });
    }

    private void insert(String table, long start, long end, String document, int amount, String title, String type) {
        jdbc.update(("INSERT INTO %s (start_timestamp, end_timestamp, document, amount, doc_title, record_type) "
            + "VALUES (?, ?, ?, ?, ?, ?)").formatted(table), start, end, document, amount, title, type);
    }
}
