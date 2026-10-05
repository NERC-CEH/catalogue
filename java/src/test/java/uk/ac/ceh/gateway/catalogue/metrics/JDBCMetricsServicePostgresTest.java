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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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
 * <p>Same image as staging's {@code metrics-db} and the compose service. Skipped when Docker is not
 * available locally, but never in CI (where {@code CI} is set): there a missing Docker fails the test
 * instead, because a skip shows as a green pipeline and this is the only place the SQL meets the real
 * engine. {@code JDBCMetricsServiceTest} still guards the bind types and the schema on H2.</p>
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
        if (System.getenv("CI") == null) {
            assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is not available");
        }
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
        service = new JDBCMetricsService(dataSource, documentRepository);
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
     * The write path goes through {@code SimpleJdbcInsert}, which reads column metadata from the driver;
     * identifier casing and metadata behaviour differ between H2 and PostgreSQL.
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
     * number and the report's date filters stopped finding it. Written with {@code SimpleJdbcInsert}, as
     * {@code syncDB} writes, because it binds by the column metadata: that is what made the narrowing
     * silent, where a plain {@code setObject(Long)} would have failed with "integer out of range".
     */
    @Test
    void timestampsAfter2038SurviveARoundTrip() {
        //given a window in 2040
        val start = Instant.parse("2040-01-01T00:00:00Z").getEpochSecond();
        val end = Instant.parse("2040-01-01T01:00:00Z").getEpochSecond();

        //when it is written
        new SimpleJdbcInsert(dataSource).withTableName("views").execute(Map.of(
            "start_timestamp", start, "end_timestamp", end, "amount", 1,
            "document", "abcd1", "doc_title", "test1", "record_type", "a"));

        //then it reads back unchanged, and the date filters still find it
        assertThat(jdbc.queryForObject("SELECT start_timestamp FROM views", Long.class), equalTo(start));
        assertThat(service.getMetricsReport(Instant.ofEpochSecond(start), Instant.ofEpochSecond(end), null, null, null, null, null).toString(),
            equalTo("[{document=abcd1, docTitle=test1, recordType=a, views=1, downloads=0}]"));
    }

    private void insert(String table, long start, long end, String document, int amount, String title, String type) {
        jdbc.update(("INSERT INTO %s (start_timestamp, end_timestamp, document, amount, doc_title, record_type) "
            + "VALUES (?, ?, ?, ?, ?, ?)").formatted(table), start, end, document, amount, title, type);
    }
}
