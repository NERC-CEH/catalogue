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
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;
import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * {@link JDBCMetricsService} against a real PostgreSQL whose schema is created by the Flyway
 */
@ExtendWith(MockitoExtension.class)
class JDBCMetricsServiceTest {
    private static final String IMAGE = "postgres:14.24-alpine";

    private static final String TEST_DOCUMENT = "123e4567-e89b-12d3-a456-426614174000";
    private static final String OTHER_DOCUMENT = "123e4567-e89b-12d3-a456-426614174999";
    private static final String DOC1 = "abcd0001-0000-4000-8000-000000000001";
    private static final String DOC2 = "abcd0002-0000-4000-8000-000000000002";
    private static final String DOC3 = "abcd0003-0000-4000-8000-000000000003";
    private static final String DOC4 = "abcd0004-0000-4000-8000-000000000004";
    private static final String TEST_IP1 = "192.0.2.1";
    private static final String TEST_IP2 = "192.0.2.2";

    private static PostgreSQLContainer postgres;
    private static DataSource postgresDataSource;

    @Mock private DocumentRepository documentRepository;
    private final MetadataDocument doc = new GeminiDocument();
    private SeverableDataSource dataSource;
    private JDBCMetricsService service;
    private JdbcTemplate jdbc;

    @BeforeAll
    static void startPostgres() {
        val externalUrl = System.getenv("METRICS_TEST_POSTGRES_URL");
        if (externalUrl != null) {
            postgresDataSource = new DriverManagerDataSource(externalUrl,
                envOr("METRICS_TEST_POSTGRES_USER", "catalogue"),
                envOr("METRICS_TEST_POSTGRES_PASSWORD", "catalogue"));
            awaitConnection(postgresDataSource, externalUrl);
        } else {
            assertThat("METRICS_TEST_POSTGRES_URL must be set in CI; see the class javadoc",
                System.getenv("CI"), is(nullValue()));
            assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is not available");
            postgres = new PostgreSQLContainer(IMAGE)
                .withDatabaseName("catalogue")
                .withUsername("catalogue")
                .withPassword("catalogue");
            postgres.start();
            postgresDataSource = new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        }
        // Only the metrics schema: anything else in a shared CI database is left alone.
        new JdbcTemplate(postgresDataSource).execute("DROP SCHEMA IF EXISTS metrics CASCADE");
    }

    @AfterAll
    static void stopPostgres() {
        if (postgres != null) {
            postgres.stop();
        }
    }

    @BeforeEach
    void setup() {
        dataSource = new SeverableDataSource(postgresDataSource);
        service = new JDBCMetricsService(dataSource, documentRepository);
        assertThat(service.ensureSchema(), is(true));
        jdbc = new JdbcTemplate(postgresDataSource);
        jdbc.execute("TRUNCATE metrics.metric_counts, metrics.documents");
        doc.setTitle("default Test Title");
        doc.setType("default dataset");
    }

    // ---------------------------------------------------------------- schema

    @Test
    void flywayCreatedTheSchemaFromV1() {
        assertThat(jdbc.queryForList(
                "SELECT version FROM metrics.flyway_schema_history WHERE success AND type = 'SQL' ORDER BY installed_rank",
                String.class),
            contains(equalTo("1")));
    }

    /** The database is shared: everything Flyway creates is in "metrics", nothing in "public". */
    @Test
    void everythingIsCreatedInTheMetricsSchemaAndNothingInPublic() {
        assertThat(jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'metrics' ORDER BY table_name",
                String.class),
            contains("documents", "flyway_schema_history", "metric_counts"));
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public'", Integer.class),
            is(0));
    }

    /** The one catalogue login sees every schema, so a same-named table elsewhere must be ignored. */
    @Test
    void aSameNamedTableInAnotherSchemaIsIgnored() {
        jdbc.execute("CREATE SCHEMA IF NOT EXISTS other");
        jdbc.execute("CREATE TABLE IF NOT EXISTS other.metric_counts (document_id uuid, metric_type text, amount integer)");
        jdbc.update("INSERT INTO other.metric_counts VALUES (?, 'view', 99)", UUID.fromString(DOC1));
        try {
            insert("view", 1721952000L, 1722038399L, DOC1, 1, "t", "a");

            assertThat(service.totalViews(DOC1), is(1));
        } finally {
            jdbc.execute("DROP SCHEMA other CASCADE");
        }
    }

    @Test
    void bothTablesHaveAPrimaryKey() {
        assertThat(primaryKeyColumns("documents"), contains(equalTo("document_id")));
        assertThat(primaryKeyColumns("metric_counts"), contains(equalTo("id")));
    }

    /** Safe for every pod to migrate on startup: an up-to-date schema is a no-op. */
    @Test
    void migratingAnUpToDateDatabaseIsANoOp() {
        assertThat(JDBCMetricsService.flyway(postgresDataSource).load().migrate().migrationsExecuted, is(0));
    }

    // ---------------------------------------------------------------- query plans

    @Test
    void totalsSumIsAnIndexOnlyScan() {
        seedBackgroundRows();
        insert("view", 1721952000L, 1722038399L, DOC1, 1, "t", "a");
        jdbc.execute("VACUUM ANALYZE metrics.metric_counts");

        val plan = explain(JDBCMetricsService.TOTAL_STATEMENT, UUID.fromString(DOC1), "view");

        assertThat(plan, containsString("Index Only Scan using idx_metric_counts_document_type"));
    }

    @Test
    void reportDateRangeUsesTheCountedFromIndex() {
        seedBackgroundRows();
        insert("view", 1721952000L, 1722038399L, DOC1, 1, "t", "a");
        jdbc.execute("VACUUM ANALYZE metrics.metric_counts");

        val plan = explain("""
            SELECT m.document_id FROM metrics.metric_counts m
            WHERE m.counted_from >= ? AND m.counted_until <= ? AND m.counted_from <= ?
            """, at(1721952000L), at(1722124800L), at(1722124800L));

        assertThat(plan, containsString("idx_metric_counts_counted_from"));
    }

    // ---------------------------------------------------------------- recording and totals

    @SneakyThrows
    @Test
    void testRecordView() {
        //given
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        //when
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.recordView(TEST_DOCUMENT, TEST_IP2);
        service.syncDB();

        //then
        assertThat(getDocumentsAndAmounts("view"), contains(contains(TEST_DOCUMENT, 2)));
    }

    @SneakyThrows
    @Test
    void testRecordDownload() {
        //given
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        //when
        service.recordDownload(TEST_DOCUMENT, TEST_IP1);
        service.recordDownload(TEST_DOCUMENT, TEST_IP1);
        service.recordDownload(TEST_DOCUMENT, TEST_IP2);
        service.syncDB();

        //then
        assertThat(getDocumentsAndAmounts("download"), contains(contains(TEST_DOCUMENT, 2)));
    }

    @SneakyThrows
    @Test
    void testCountViews() {
        val howMany = 3;
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        //given
        IntStream.range(0, howMany).forEach(_i -> {
            service.recordView(TEST_DOCUMENT, TEST_IP1);
            service.recordView(TEST_DOCUMENT, TEST_IP2);
            service.syncDB();
        });

        //when
        val amount = service.totalViews(TEST_DOCUMENT);

        //then
        assertThat(amount, equalTo(2 * howMany));
    }

    @SneakyThrows
    @Test
    void testCountDownloads() {
        val howMany = 3;
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        //given
        IntStream.range(0, howMany).forEach(_i -> {
            service.recordDownload(TEST_DOCUMENT, TEST_IP1);
            service.recordDownload(TEST_DOCUMENT, TEST_IP2);
            service.syncDB();
        });

        //when
        val amount = service.totalDownloads(TEST_DOCUMENT);

        //then
        assertThat(amount, equalTo(2 * howMany));
    }

    /**
     * A transient repository fault (the record exists but could not be read) keeps the count;
     * {@code doc_title} NULL means "never resolved" and the nightly refresh fills it in.
     */
    @SneakyThrows
    @Test
    void aCountIsKeptWhenTheRecordExistsButCannotBeReadAndItsTitleIsResolvedLater() {
        given(documentRepository.read(TEST_DOCUMENT))
            .willThrow(new RuntimeException("repository down"))
            .willReturn(document("resolved", "dataset"));
        service.recordView(TEST_DOCUMENT, TEST_IP1);

        service.syncDB();
        assertThat(service.totalViews(TEST_DOCUMENT), equalTo(1));
        assertThat(jdbc.queryForObject("SELECT doc_title IS NULL FROM metrics.documents", Boolean.class), is(true));

        service.updateDB();
        assertThat(jdbc.queryForList("SELECT doc_title FROM metrics.documents", String.class), containsInAnyOrder("resolved"));
    }

    /** #246: an unknown UUID (e.g. a random /download/{uuid}) must not create a documents row or a count. */
    @SneakyThrows
    @Test
    void aCountForARecordThatDoesNotExistIsDropped() {
        given(documentRepository.read(TEST_DOCUMENT)).willThrow(notFound(TEST_DOCUMENT));
        service.recordDownload(TEST_DOCUMENT, TEST_IP1);
        service.recordView(TEST_DOCUMENT, TEST_IP1);

        service.syncDB();

        assertThat(jdbc.queryForObject("SELECT count(*) FROM metrics.metric_counts", Integer.class), is(0));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM metrics.documents", Integer.class), is(0));
    }

    /** The upsert's coalesce: a failed repository read never replaces a known title with NULL. */
    @SneakyThrows
    @Test
    void anUnresolvedTitleNeverOverwritesAKnownOne() {
        given(documentRepository.read(TEST_DOCUMENT))
            .willReturn(document("known", "dataset"))
            .willThrow(new RuntimeException("repository down"));
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();
        service.recordView(TEST_DOCUMENT, TEST_IP2);
        service.syncDB();

        assertThat(jdbc.queryForList("SELECT doc_title FROM metrics.documents", String.class), containsInAnyOrder("known"));
        assertThat(service.totalViews(TEST_DOCUMENT), equalTo(2));
    }

    /** {@code document_id} is uuid. The UUID check comes before the repository read, so it is never called. */
    @Test
    void nonUuidDocumentIdsReadAsEmptyAndAreNotWritten() {
        service.recordView("not-a-uuid", TEST_IP1);

        service.syncDB();

        assertThat(jdbc.queryForObject("SELECT count(*) FROM metrics.metric_counts", Integer.class), is(0));
        assertThat(service.totalViews("not-a-uuid"), equalTo(0));
        assertThat(service.hasMetricsFor("not-a-uuid"), is(false));
        assertThat(service.deleteMetricsFor("not-a-uuid"), is(false));
        verifyNoInteractions(documentRepository);
    }

    // ---------------------------------------------------------------- outages

    /**
     * A view counter is read while rendering a record page, so a database failure must come back as an
     * absent count, leaving the page to render without it, rather than 500ing the page.
     */
    @Test
    void totalsAreAbsentRatherThanThrowingWhenTheDatabaseIsUnavailable() {
        //given the database cannot be reached
        dataSource.sever();

        //when/then no exception escapes, and the counts report themselves as unavailable
        assertThat(service.totalViews(TEST_DOCUMENT), is(nullValue()));
        assertThat(service.totalDownloads(TEST_DOCUMENT), is(nullValue()));
    }

    /**
     * The startup-failure policy: the catalogue must start when the metrics database is down.
     */
    @Test
    void serviceStartsWhenTheDatabaseIsUnreachable() {
        //given a database that is down before the service is created
        val severed = new SeverableDataSource(postgresDataSource);
        severed.sever();

        //when the service is constructed and the startup hook runs
        val startingService = new JDBCMetricsService(severed, documentRepository);
        val attemptsDuringConstruction = severed.connectionAttempts();
        startingService.initialiseSchemaOnStartup();

        //then neither threw, the constructor did no I/O, and the schema is reported as not ready
        assertThat(attemptsDuringConstruction, is(0));
        assertThat(startingService.ensureSchema(), is(false));
        assertThat(startingService.totalViews(TEST_DOCUMENT), is(nullValue()));
    }

    /**
     * A database that was down at startup must be migrated once it is back, without a restart. The
     * hourly sync is the path guaranteed to run, so recovery is asserted through it.
     */
    @SneakyThrows
    @Test
    void schemaIsMigratedOnFirstUseAfterTheDatabaseComesBack() {
        //given the service started while the database was down
        val severed = new SeverableDataSource(postgresDataSource);
        severed.sever();
        val recoveringService = new JDBCMetricsService(severed, documentRepository);
        recoveringService.initialiseSchemaOnStartup();
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        recoveringService.recordView(TEST_DOCUMENT, TEST_IP1);

        //when the database comes back and the hourly sync runs
        severed.restore();
        recoveringService.syncDB();

        //then the schema is ready and the count was written
        assertThat(recoveringService.ensureSchema(), is(true));
        assertThat(recoveringService.totalViews(TEST_DOCUMENT), equalTo(1));
    }

    @SneakyThrows
    @Test
    void syncKeepsCountsInMemoryUntilTheSchemaCanBeMigrated() {
        //given a service that started while the database was down, and has recorded a view and a download
        val severed = new SeverableDataSource(postgresDataSource);
        severed.sever();
        val startingService = new JDBCMetricsService(severed, documentRepository);
        startingService.initialiseSchemaOnStartup();
        startingService.recordView(TEST_DOCUMENT, TEST_IP1);
        startingService.recordDownload(TEST_DOCUMENT, TEST_IP1);

        //when the hourly sync runs with the database still down
        startingService.syncDB();

        //then it gave up at the migration attempt, without reading any documents
        verifyNoInteractions(documentRepository);

        //when the database comes back and the next sync runs
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        severed.restore();
        startingService.syncDB();

        //then the counts recorded during the outage were written
        assertThat(startingService.totalViews(TEST_DOCUMENT), equalTo(1));
        assertThat(startingService.totalDownloads(TEST_DOCUMENT), equalTo(1));
    }

    // ---------------------------------------------------------------- sync windows

    /**
     * Regression test for the epoch-0 window: the first sync after every start used to write a window
     * opening on 1 January 1970. The first window must open when the service was created.
     */
    @SneakyThrows
    @Test
    void firstSyncAfterConstructionWritesATruthfulWindowNotTheEpoch() {
        //given a service created at a known instant, and a view recorded during its first hour
        val bootedAt = Instant.parse("2026-10-19T09:00:00Z");
        val clock = new MutableClock(bootedAt);
        val freshService = new JDBCMetricsService(dataSource, documentRepository, clock);
        assertThat(freshService.ensureSchema(), is(true));
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        freshService.recordView(TEST_DOCUMENT, TEST_IP1);

        //when the first hourly sync runs
        clock.advance(Duration.ofHours(1));
        freshService.syncDB();

        //then the row covers exactly that hour, starting at construction, not at 0
        assertThat(getWindows("view"), contains(
            contains(bootedAt.getEpochSecond(), bootedAt.plus(Duration.ofHours(1)).getEpochSecond())));
    }

    /** Each sync's window starts where the previous one ended: no gaps, no overlaps. */
    @SneakyThrows
    @Test
    void consecutiveSyncWindowsAreContiguous() {
        //given
        val bootedAt = Instant.parse("2026-10-19T09:00:00Z");
        val clock = new MutableClock(bootedAt);
        val freshService = new JDBCMetricsService(dataSource, documentRepository, clock);
        assertThat(freshService.ensureSchema(), is(true));
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);

        //when two hourly syncs run, each with a view recorded before it
        freshService.recordView(TEST_DOCUMENT, TEST_IP1);
        clock.advance(Duration.ofHours(1));
        freshService.syncDB();
        freshService.recordView(TEST_DOCUMENT, TEST_IP2);
        clock.advance(Duration.ofHours(1));
        freshService.syncDB();

        //then
        val t0 = bootedAt.getEpochSecond();
        assertThat(getWindows("view"), contains(
            contains(t0, t0 + 3600),
            contains(t0 + 3600, t0 + 7200)));
    }

    /** A window held over an outage must still open at construction, not at 0. */
    @SneakyThrows
    @Test
    void windowHeldOverAnOutageStillStartsAtConstruction() {
        //given a service that started while its database was down
        val severed = new SeverableDataSource(postgresDataSource);
        severed.sever();
        val bootedAt = Instant.parse("2026-10-19T09:00:00Z");
        val clock = new MutableClock(bootedAt);
        val startingService = new JDBCMetricsService(severed, documentRepository, clock);
        startingService.initialiseSchemaOnStartup();
        startingService.recordView(TEST_DOCUMENT, TEST_IP1);

        //when the first sync is skipped, and the second runs once the database is back
        clock.advance(Duration.ofHours(1));
        startingService.syncDB();
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        severed.restore();
        clock.advance(Duration.ofHours(1));
        startingService.syncDB();

        //then one row spans both hours, from construction
        val t0 = bootedAt.getEpochSecond();
        assertThat(getWindows("view"), contains(contains(t0, t0 + 7200)));
    }

    /** Epoch seconds pass 2^31 in January 2038; the timestamptz columns must store later windows intact. */
    @SneakyThrows
    @Test
    void windowsAfter2038AreStoredIntact() {
        //given a service running in 2040
        val bootedAt = Instant.parse("2040-01-01T00:00:00Z");
        val clock = new MutableClock(bootedAt);
        val freshService = new JDBCMetricsService(dataSource, documentRepository, clock);
        assertThat(freshService.ensureSchema(), is(true));
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        freshService.recordView(TEST_DOCUMENT, TEST_IP1);

        //when
        clock.advance(Duration.ofHours(1));
        freshService.syncDB();

        //then it reads back unchanged, and the report's date filters still find it
        val t0 = bootedAt.getEpochSecond();
        assertThat(getWindows("view"), contains(contains(t0, t0 + 3600)));
        assertThat(freshService.getMetricsReport(bootedAt, bootedAt.plus(Duration.ofHours(1)),
            null, null, null, null, null), hasSize(1));
    }

    // ---------------------------------------------------------------- nightly title refresh

    @SneakyThrows
    @Test
    void testUpdateView() {
        //given
        given(documentRepository.read(anyString())).willReturn(doc);

        //when
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();
        assertThat(getTitleAndType(), contains(contains(doc.getTitle(), doc.getType())));

        doc.setTitle("updated Test Title");
        doc.setType("updated dataset");
        service.updateDB();

        //then
        assertThat(getTitleAndType(), contains(contains(doc.getTitle(), doc.getType())));
    }

    @SneakyThrows
    @Test
    void testUpdateDownload() {
        //given
        given(documentRepository.read(anyString())).willReturn(doc);

        //when
        service.recordDownload(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();
        assertThat(getTitleAndType(), contains(contains(doc.getTitle(), doc.getType())));

        doc.setTitle("updated Test Title");
        doc.setType("updated dataset");
        service.updateDB();

        //then
        assertThat(getTitleAndType(), contains(contains(doc.getTitle(), doc.getType())));
    }

    /**
     * A repository failure for one document must not abort the nightly title refresh or escape the
     * scheduler; the row keeps the title it already had until the next run.
     */
    @SneakyThrows
    @Test
    void testUpdateViewThrows() {
        //given the second repository read fails
        given(documentRepository.read(anyString())).willReturn(doc).willThrow(new RuntimeException("Oops"));
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();

        //when
        service.updateDB();

        //then the failure is swallowed and the existing row is left untouched
        verify(documentRepository, times(2)).read(anyString());
        assertThat(getTitleAndType(), contains(contains("default Test Title", "default dataset")));
    }

    @SneakyThrows
    @Test
    void testUpdateDownloadThrows() {
        //given the second repository read fails
        given(documentRepository.read(anyString())).willReturn(doc).willThrow(new RuntimeException("Oops"));
        service.recordDownload(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();

        //when
        service.updateDB();

        //then the failure is swallowed and the existing row is left untouched
        verify(documentRepository, times(2)).read(anyString());
        assertThat(getTitleAndType(), contains(contains("default Test Title", "default dataset")));
    }

    /** A record deleted from the catalogue is marked removed, keeps its counts, and is not re-read nightly. */
    @SneakyThrows
    @Test
    void updateDBMarksADeletedRecordRemovedAndStopsRereadingIt() {
        //given a record with a count, later deleted from the catalogue
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc).willThrow(notFound(TEST_DOCUMENT));
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();

        //when the nightly refresh runs twice
        service.updateDB();
        service.updateDB();

        //then it was marked removed on the first run, kept its count and title, and was not read again
        assertThat(jdbc.queryForObject("SELECT removed_at IS NOT NULL FROM metrics.documents", Boolean.class), is(true));
        assertThat(service.totalViews(TEST_DOCUMENT), equalTo(1));
        assertThat(getTitleAndType(), contains(contains("default Test Title", "default dataset")));
        verify(documentRepository, times(2)).read(TEST_DOCUMENT);
    }

    /** A transient failure is not a deletion: removed_at stays NULL and the record is retried next night. */
    @SneakyThrows
    @Test
    void updateDBDoesNotMarkARecordRemovedOnATransientFailure() {
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc).willThrow(new RuntimeException("repository down"));
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();

        service.updateDB();

        assertThat(jdbc.queryForObject("SELECT removed_at IS NULL FROM metrics.documents", Boolean.class), is(true));
    }

    /** A record that comes back (restored, re-published) is un-removed by its next count. */
    @SneakyThrows
    @Test
    void aCountForARecordThatCameBackClearsRemovedAt() {
        given(documentRepository.read(TEST_DOCUMENT))
            .willReturn(doc).willThrow(notFound(TEST_DOCUMENT)).willReturn(doc);
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();
        service.updateDB();

        service.recordView(TEST_DOCUMENT, TEST_IP2);
        service.syncDB();

        assertThat(jdbc.queryForObject("SELECT removed_at IS NULL FROM metrics.documents", Boolean.class), is(true));
        assertThat(service.totalViews(TEST_DOCUMENT), equalTo(2));
    }

    // ---------------------------------------------------------------- flyway validation

    /**
     * An edited applied migration is a deployment fault, not an outage: ensureSchema must stop retrying
     * (no further connection attempts) instead of looping forever behind a WARN.
     */
    @Test
    void aFlywayValidationFailureStopsRetrying() {
        jdbc.update("UPDATE metrics.flyway_schema_history SET checksum = checksum + 1 WHERE version = '1'");
        try {
            val counting = new SeverableDataSource(postgresDataSource);
            val validating = new JDBCMetricsService(counting, documentRepository);

            assertThat(validating.ensureSchema(), is(false));
            val attemptsAfterFirstFailure = counting.connectionAttempts();
            assertThat(validating.ensureSchema(), is(false));
            validating.syncDB();

            assertThat(counting.connectionAttempts(), is(attemptsAfterFirstFailure));
        } finally {
            jdbc.update("UPDATE metrics.flyway_schema_history SET checksum = checksum - 1 WHERE version = '1'");
        }
    }

    /** The pinned V1 checksum is the one Flyway itself recorded (see FlywayMigrationChecksumTest). */
    @Test
    void flywayRecordedThePinnedV1Checksum() {
        assertThat(jdbc.queryForObject(
                "SELECT checksum FROM metrics.flyway_schema_history WHERE version = '1'", Integer.class),
            is(FlywayMigrationChecksumTest.V1_CHECKSUM));
    }

    // ---------------------------------------------------------------- hasMetricsFor / deleteMetricsFor

    @Test
    void hasMetricsForIsFalseWhenNothingIsRecorded() {
        assertThat(service.hasMetricsFor(TEST_DOCUMENT), is(false));
    }

    @SneakyThrows
    @Test
    void hasMetricsForIsTrueWhenOnlyAViewIsRecorded() {
        //given
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();

        //when/then
        assertThat(service.hasMetricsFor(TEST_DOCUMENT), is(true));
    }

    @SneakyThrows
    @Test
    void hasMetricsForIsTrueWhenOnlyADownloadIsRecorded() {
        //given
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        service.recordDownload(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();

        //when/then
        assertThat(service.hasMetricsFor(TEST_DOCUMENT), is(true));
    }

    /** Deletion is by document only: another document's counts and documents row must survive. */
    @SneakyThrows
    @Test
    void deleteMetricsForRemovesOnlyTheGivenDocumentsRows() {
        //given
        given(documentRepository.read(anyString())).willReturn(doc);
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.recordDownload(TEST_DOCUMENT, TEST_IP1);
        service.recordView(OTHER_DOCUMENT, TEST_IP1);
        service.syncDB();

        //when
        boolean result = service.deleteMetricsFor(TEST_DOCUMENT);

        //then
        assertThat(result, is(true));
        assertThat(getDocumentsAndAmounts("view"), contains(contains(OTHER_DOCUMENT, 1)));
        assertThat(getDocumentsAndAmounts("download"), is(empty()));
        assertThat(service.hasMetricsFor(TEST_DOCUMENT), is(false));
        assertThat(jdbc.queryForList("SELECT document_id::text FROM metrics.documents", String.class),
            contains(equalTo(OTHER_DOCUMENT)));
    }

    @Test
    void deleteMetricsForReturnsFalseWhenThereWasNothingToDelete() {
        assertThat(service.deleteMetricsFor(TEST_DOCUMENT), is(false));
    }

    // ---------------------------------------------------------------- report

    @Test
    void testGetMetricsReport() {
        insert("view", 1721952000L, 1722038399L, DOC1, 1, "test1", "a");      // 26 July 2024
        insert("view", 1722038400L, 1722124799L, DOC2, 2, "test2", "b");      // 27 July 2024
        insert("view", 1722124800L, 1722211199L, DOC3, 3, "test3", "c");      // 28 July 2024
        insert("download", 1721952000L, 1722038399L, DOC2, 1, "test2", "b");  // 26 July 2024
        insert("download", 1722038400L, 1722124799L, DOC3, 2, "test3", "c");  // 27 July 2024
        insert("download", 1722124800L, 1722211199L, DOC4, 3, "test4", "d");  // 28 July 2024

        List<String> recordType = Arrays.asList("a", "c");
        String orderBy = "views";
        String ordering = "descending";
        String docId = "abcd0001";
        Integer noOfRecords = 1;

        //when/then no record in the supplied dates
        String result = service.getMetricsReport(Instant.parse("2024-07-29T00:00:00Z"), Instant.parse("2024-07-29T23:59:59Z"),
            orderBy, ordering, recordType, docId, noOfRecords).toString();
        assertThat(result, equalTo("[]"));

        Instant startDate = Instant.parse("2024-07-26T00:00:00Z");
        Instant endDate = Instant.parse("2024-07-27T23:59:59Z");
        result = service.getMetricsReport(startDate, null, null, null, null, null, null).toString();
        assertThat(result, equalTo("[" +
            row(DOC1, "test1", "a", 1, 0) + ", " +
            row(DOC2, "test2", "b", 2, 1) + ", " +
            row(DOC3, "test3", "c", 3, 2) + ", " +
            row(DOC4, "test4", "d", 0, 3) + "]"));

        result = service.getMetricsReport(startDate, endDate, null, null, null, null, null).toString();
        assertThat(result, equalTo("[" +
            row(DOC1, "test1", "a", 1, 0) + ", " +
            row(DOC2, "test2", "b", 2, 1) + ", " +
            row(DOC3, "test3", "c", 0, 2) + "]"));

        result = service.getMetricsReport(startDate, endDate, orderBy, null, null, null, null).toString();
        assertThat(result, equalTo("[" +
            row(DOC3, "test3", "c", 0, 2) + ", " +
            row(DOC1, "test1", "a", 1, 0) + ", " +
            row(DOC2, "test2", "b", 2, 1) + "]"));

        result = service.getMetricsReport(startDate, endDate, orderBy, ordering, null, null, null).toString();
        assertThat(result, equalTo("[" +
            row(DOC2, "test2", "b", 2, 1) + ", " +
            row(DOC1, "test1", "a", 1, 0) + ", " +
            row(DOC3, "test3", "c", 0, 2) + "]"));

        orderBy = "downloads";
        result = service.getMetricsReport(startDate, endDate, orderBy, ordering, null, null, null).toString();
        assertThat(result, equalTo("[" +
            row(DOC3, "test3", "c", 0, 2) + ", " +
            row(DOC2, "test2", "b", 2, 1) + ", " +
            row(DOC1, "test1", "a", 1, 0) + "]"));

        result = service.getMetricsReport(startDate, endDate, orderBy, ordering, recordType, null, null).toString();
        assertThat(result, equalTo("[" +
            row(DOC3, "test3", "c", 0, 2) + ", " +
            row(DOC1, "test1", "a", 1, 0) + "]"));

        result = service.getMetricsReport(startDate, endDate, orderBy, ordering, recordType, docId, null).toString();
        assertThat(result, equalTo("[" + row(DOC1, "test1", "a", 1, 0) + "]"));

        docId = "abcd";
        result = service.getMetricsReport(startDate, endDate, orderBy, ordering, recordType, docId, noOfRecords).toString();
        assertThat(result, equalTo("[" + row(DOC3, "test3", "c", 0, 2) + "]"));
    }

    /** A document ID pasted in upper case must still match (ILIKE). */
    @Test
    void reportDocumentFilterIgnoresCase() {
        //given a view of a lower-case document ID
        insert("view", 1721952000L, 1722038399L, DOC1, 1, "test1", "a");

        //when the report is filtered on part of the ID in upper case
        val result = service.getMetricsReport(null, null, null, null, null, "ABCD0001", null);

        //then the document is still found
        assertThat(result.toString(), equalTo("[" + row(DOC1, "test1", "a", 1, 0) + "]"));
    }

    /** LIKE metacharacters in the document filter match literally, not as wildcards. */
    @Test
    void reportDocumentFilterTreatsWildcardsLiterally() {
        insert("view", 1721952000L, 1722038399L, DOC1, 1, "test1", "a");

        assertThat(service.getMetricsReport(null, null, null, null, null, "_", null), is(empty()));
        assertThat(service.getMetricsReport(null, null, null, null, null, "%", null), is(empty()));
    }

    /**
     * The report always has a LIMIT, and PostgreSQL's grouping order is not defined, so a count ordering
     * is broken by document: a tie at the limit is decided the same way every time.
     */
    @Test
    void reportTiesAtTheLimitAreBrokenByDocument() {
        //given two documents with the same number of views, inserted in reverse document order
        insert("view", 1721952000L, 1722038399L, DOC2, 1, "test2", "b");
        insert("view", 1721952000L, 1722038399L, DOC1, 1, "test1", "a");

        //when ordered by views, descending, limited to one
        val result = service.getMetricsReport(null, null, "views", "descending", null, null, 1);

        //then the lower document wins the tie
        assertThat(result.toString(), equalTo("[" + row(DOC1, "test1", "a", 1, 0) + "]"));
    }

    /**
     * An unrecognised orderBy falls back to document order (ordering still applies); it must never reach
     * the ORDER BY %s. DOC1 has more views, so ordering by views descending would put it first.
     */
    @Test
    void reportUnknownOrderByFallsBackToDocumentOrder() {
        insert("view", 1721952000L, 1722038399L, DOC1, 5, "test1", "a");
        insert("view", 1721952000L, 1722038399L, DOC2, 1, "test2", "b");

        val result = service.getMetricsReport(null, null, "doc_title; DROP TABLE metrics.documents --",
            "descending", null, null, null);

        assertThat(result.toString(), equalTo("[" +
            row(DOC2, "test2", "b", 1, 0) + ", " + row(DOC1, "test1", "a", 5, 0) + "]"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM metrics.documents", Integer.class), is(2));
    }

    /** With no noOfRecords (or a negative one) the report is capped at 100 rows. */
    @Test
    void reportDefaultsToALimitOf100() {
        jdbc.execute("""
            WITH d AS (
                INSERT INTO metrics.documents (document_id, doc_title, record_type)
                SELECT gen_random_uuid(), 'bulk', 'dataset' FROM generate_series(1, 101)
                RETURNING document_id
            )
            INSERT INTO metrics.metric_counts (document_id, metric_type, counted_from, counted_until, amount)
            SELECT d.document_id, 'view', timestamptz '2024-07-26', timestamptz '2024-07-27', 1 FROM d
            """);

        assertThat(service.getMetricsReport(null, null, null, null, null, null, null), hasSize(100));
        assertThat(service.getMetricsReport(null, null, null, null, null, null, -1), hasSize(100));
        assertThat(service.getMetricsReport(null, null, null, null, null, null, 101), hasSize(101));
    }

    /**
     * A backslash is escaped too, so it matches literally. Unescaped, "\\a" would be LIKE's escaped "a"
     * and "abcd\\0001" would be "abcd0001", and both would wrongly match DOC1.
     */
    @Test
    void reportDocumentFilterTreatsBackslashLiterally() {
        insert("view", 1721952000L, 1722038399L, DOC1, 1, "test1", "a");

        assertThat(service.getMetricsReport(null, null, null, null, null, "\\a", null), is(empty()));
        assertThat(service.getMetricsReport(null, null, null, null, null, "abcd\\0001", null), is(empty()));
    }

    // ---------------------------------------------------------------- shutdown flush

    /** Counts recorded since the last hourly sync must be written at shutdown. */
    @SneakyThrows
    @Test
    void shutdownFlushWritesCountsAccumulatedSinceTheLastSync() {
        //given a service that has synced once, then recorded more activity
        val bootedAt = Instant.parse("2026-10-19T09:00:00Z");
        val clock = new MutableClock(bootedAt);
        val freshService = new JDBCMetricsService(dataSource, documentRepository, clock);
        freshService.setShutdownFlushTimeoutMillis(8_000);
        assertThat(freshService.ensureSchema(), is(true));
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        freshService.recordView(TEST_DOCUMENT, TEST_IP1);
        clock.advance(Duration.ofHours(1));
        freshService.syncDB();
        freshService.recordView(TEST_DOCUMENT, TEST_IP1);
        freshService.recordView(TEST_DOCUMENT, TEST_IP2);
        freshService.recordDownload(TEST_DOCUMENT, TEST_IP1);

        //when the context closes 20 minutes into the next window
        clock.advance(Duration.ofMinutes(20));
        freshService.flushOnShutdown();

        //then the partial window was written rather than dropped, contiguous with the hourly one
        val t0 = bootedAt.getEpochSecond();
        assertThat(getWindows("view"), contains(
            contains(t0, t0 + 3600),
            contains(t0 + 3600, t0 + 3600 + 1200)));
        assertThat(getWindows("download"), contains(contains(t0 + 3600, t0 + 3600 + 1200)));
        assertThat(freshService.totalViews(TEST_DOCUMENT), equalTo(3));
        assertThat(freshService.totalDownloads(TEST_DOCUMENT), equalTo(1));
    }

    /** The common case at shutdown is a clean map; it must not cost a connection to the database. */
    @Test
    void shutdownFlushWithNothingPendingDoesNotTouchTheDatabase() {
        //given nothing recorded since the last sync
        service.setShutdownFlushTimeoutMillis(8_000);
        val attemptsBefore = dataSource.connectionAttempts();

        //when
        service.flushOnShutdown();

        //then
        assertThat(dataSource.connectionAttempts(), is(attemptsBefore));
        verifyNoInteractions(documentRepository);
    }

    /**
     * Against a slow database the flush must give up before the grace period runs out and the pod is
     * SIGKILLed, and must stop between documents, never mid-row.
     */
    @SneakyThrows
    @Test
    void shutdownFlushStopsStartingWritesOnceItsBudgetIsSpent() {
        //given three documents pending, each slower to read than the whole budget
        List.of(DOC1, DOC2, DOC3).forEach(d -> service.recordView(d, TEST_IP1));
        given(documentRepository.read(anyString())).willAnswer(invocation -> {
            Thread.sleep(150);
            return doc;
        });
        service.setShutdownFlushTimeoutMillis(100);

        //when
        service.flushOnShutdown();

        //then the first was written, and nothing was started after the deadline
        verify(documentRepository, times(1)).read(anyString());
        assertThat(getDocumentsAndAmounts("view"), hasSize(1));
    }

    /** Never throws: a broken metrics database must not interfere with destroying the rest of the context. */
    @Test
    void shutdownFlushAgainstAnUnreachableDatabaseDoesNotThrow() {
        //given a service whose database has been down since it started
        val severed = new SeverableDataSource(postgresDataSource);
        severed.sever();
        val startingService = new JDBCMetricsService(severed, documentRepository);
        startingService.setShutdownFlushTimeoutMillis(8_000);
        startingService.recordView(TEST_DOCUMENT, TEST_IP1);

        //when / then
        startingService.flushOnShutdown();
        verifyNoInteractions(documentRepository);
    }

    /** The property has no in-code default, so a nonsense value must stop startup rather than drop every count. */
    @Test
    void shutdownFlushTimeoutMustBePositive() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
            () -> service.setShutdownFlushTimeoutMillis(0));
    }

    // ---------------------------------------------------------------- helpers

    /** What GitDocumentRepository.read throws for a record that does not exist. */
    private static DocumentRepositoryException notFound(String id) {
        return new DocumentRepositoryException("Cannot read file: " + id, new GitFileNotFoundException("no such file"));
    }

    private static String envOr(String name, String fallback) {
        val value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    /** The CI service container may still be initialising; retry for up to 60s before failing. */
    @SneakyThrows
    private static void awaitConnection(DataSource ds, String url) {
        val deadline = System.nanoTime() + Duration.ofSeconds(60).toNanos();
        SQLException last;
        do {
            try (Connection ignored = ds.getConnection()) {
                return;
            } catch (SQLException e) {
                last = e;
                Thread.sleep(1000);
            }
        } while (System.nanoTime() < deadline);
        throw new IllegalStateException("Cannot connect to " + url + ": " + last.getMessage(), last);
    }

    private List<List<Object>> getDocumentsAndAmounts(String metricType) {
        return jdbc.query(
            "SELECT document_id::text, amount FROM metrics.metric_counts WHERE metric_type = ? ORDER BY id",
            (rs, i) -> List.<Object>of(rs.getString(1), rs.getInt(2)), metricType);
    }

    private List<List<Long>> getWindows(String metricType) {
        return jdbc.query("""
                SELECT extract(epoch FROM counted_from)::bigint, extract(epoch FROM counted_until)::bigint
                FROM metrics.metric_counts WHERE metric_type = ? ORDER BY counted_from
                """,
            (rs, i) -> List.of(rs.getLong(1), rs.getLong(2)), metricType);
    }

    private List<List<Object>> getTitleAndType() {
        return jdbc.query("SELECT doc_title, record_type FROM metrics.documents",
            (rs, i) -> List.<Object>of(rs.getString(1), rs.getString(2)));
    }

    private void insert(String metricType, long from, long until, String document, int amount, String title, String type) {
        val id = UUID.fromString(document);
        jdbc.update("INSERT INTO metrics.documents (document_id, doc_title, record_type) VALUES (?, ?, ?) ON CONFLICT DO NOTHING",
            id, title, type);
        jdbc.update("INSERT INTO metrics.metric_counts (document_id, metric_type, counted_from, counted_until, amount) VALUES (?, ?, ?, ?, ?)",
            id, metricType, at(from), at(until), amount);
    }

    private void seedBackgroundRows() {
        jdbc.execute("""
            WITH d AS (
                INSERT INTO metrics.documents (document_id, doc_title, record_type)
                SELECT gen_random_uuid(), 'background', 'dataset' FROM generate_series(1, 200)
                RETURNING document_id
            )
            INSERT INTO metrics.metric_counts (document_id, metric_type, counted_from, counted_until, amount)
            SELECT d.document_id, t.metric_type, ts, ts + interval '1 hour', 1
            FROM d
            CROSS JOIN (VALUES ('view'), ('download')) AS t(metric_type)
            CROSS JOIN generate_series(timestamptz '2024-01-01', timestamptz '2024-01-02', interval '1 hour') AS ts
            """);
    }

    private List<String> primaryKeyColumns(String table) {
        return jdbc.queryForList("""
            SELECT a.attname FROM pg_index i
            JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY (i.indkey)
            WHERE i.indrelid = ?::regclass AND i.indisprimary
            """, String.class, "metrics." + table);
    }

    /** EXPLAIN on one connection with seq scans off, so a tiny table cannot hide a missing index. */
    private String explain(String sql, Object... args) {
        return jdbc.execute((ConnectionCallback<String>) connection -> {
            try (val statement = connection.createStatement()) {
                statement.execute("SET enable_seqscan = off");
            }
            try {
                val single = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
                return String.join("\n", single.queryForList("EXPLAIN " + sql, String.class, args));
            } finally {
                try (val statement = connection.createStatement()) {
                    statement.execute("RESET enable_seqscan");
                }
            }
        });
    }

    private static OffsetDateTime at(long epochSecond) {
        return Instant.ofEpochSecond(epochSecond).atOffset(ZoneOffset.UTC);
    }

    private static String row(String document, String title, String type, int views, int downloads) {
        return "{document=%s, docTitle=%s, recordType=%s, views=%d, downloads=%d}"
            .formatted(document, title, type, views, downloads);
    }

    private static MetadataDocument document(String title, String type) {
        MetadataDocument document = new GeminiDocument();
        document.setTitle(title);
        document.setType(type);
        return document;
    }

    /** A clock the test moves by hand, so window timestamps can be asserted exactly. */
    private static class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration by) {
            now = now.plus(by);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    /**
     * Pass-through to the database until {@link #sever()} is called, after which every connection attempt
     * fails as it would against an unreachable host or an exhausted pool.
     */
    private static class SeverableDataSource extends AbstractDataSource {
        private final DataSource delegate;
        private volatile boolean severed;
        private int connectionAttempts;

        SeverableDataSource(DataSource delegate) {
            this.delegate = delegate;
        }

        void sever() {
            this.severed = true;
        }

        void restore() {
            this.severed = false;
        }

        int connectionAttempts() {
            return connectionAttempts;
        }

        @Override
        public Connection getConnection() throws SQLException {
            connectionAttempts++;
            if (severed) {
                throw new SQLException("Connection to metrics database refused");
            }
            return delegate.getConnection();
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            connectionAttempts++;
            if (severed) {
                throw new SQLException("Connection to metrics database refused");
            }
            return delegate.getConnection(username, password);
        }
    }
}
