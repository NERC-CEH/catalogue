package uk.ac.ceh.gateway.catalogue.metrics;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static uk.ac.ceh.gateway.catalogue.metrics.JDBCMetricsService.DOWNLOAD_TOTALS_CACHE;
import static uk.ac.ceh.gateway.catalogue.metrics.JDBCMetricsService.VIEW_TOTALS_CACHE;

/**
 * The PostgreSQL counterpart of {@link SqliteMetricsCountCachingTest}: proves that {@link JDBCMetricsService}'s
 * view and download totals are cached (they are read while a record page renders), that the two caches
 * are kept separate, and that an unavailable ({@code null}) total is never cached.
 *
 * <p>Runs against the same database as {@link JDBCMetricsServiceTest}: {@code METRICS_TEST_POSTGRES_URL}
 * (with optional {@code METRICS_TEST_POSTGRES_USER}/{@code _PASSWORD}) in CI, otherwise a Testcontainers
 * PostgreSQL. The schema is created by Flyway via {@link JDBCMetricsService#ensureSchema()}.</p>
 */
@SpringJUnitConfig(MetricsCountCachingTest.Config.class)
@TestPropertySource(properties = "metrics.shutdown-flush.timeout-millis=8000")
class MetricsCountCachingTest {
    private static final String IMAGE = "postgres:14.24-alpine";
    private static final String TEST_DOCUMENT = "123e4567-e89b-12d3-a456-426614174000";
    private static final OffsetDateTime FROM = OffsetDateTime.of(2024, 7, 26, 0, 0, 0, 0, ZoneOffset.UTC);

    private static PostgreSQLContainer postgres;
    private static DataSource postgresDataSource;

    @Autowired private MetricsService service;
    @Autowired private SeverableDataSource dataSource;
    @Autowired private CacheManager cacheManager;

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
            assertThat("METRICS_TEST_POSTGRES_URL must be set in CI",
                System.getenv("CI"), is(nullValue()));
            assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is not available");
            postgres = new PostgreSQLContainer(IMAGE)
                .withDatabaseName("catalogue")
                .withUsername("catalogue")
                .withPassword("catalogue");
            postgres.start();
            postgresDataSource = new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        }
    }

    @AfterAll
    static void stopPostgres() {
        if (postgres != null) {
            postgres.stop();
        }
    }

    /**
     * The context is shared across methods, so both the rows and the cache entries have to be reset
     * here — a leftover cached total would make a later test pass or fail for the wrong reason.
     */
    @BeforeEach
    void reset() {
        dataSource.restore();
        jdbc = new JdbcTemplate(postgresDataSource);
        jdbc.execute("TRUNCATE metrics.metric_counts, metrics.documents");
        cacheManager.getCache(VIEW_TOTALS_CACHE).clear();
        cacheManager.getCache(DOWNLOAD_TOTALS_CACHE).clear();
    }

    /**
     * Inserting a second row behind the cache's back and still seeing the first total is what proves the
     * database was not consulted again — no mocking of the JDBC layer required.
     */
    @Test
    void repeatedViewCountsAreServedFromCacheRatherThanTheDatabase() {
        //given a document with one recorded view
        insert("view", TEST_DOCUMENT, 1);
        assertThat(service.totalViews(TEST_DOCUMENT), equalTo(1));

        //when more views land in the database directly
        insert("view", TEST_DOCUMENT, 40);

        //then the cached total is served, unchanged
        assertThat(service.totalViews(TEST_DOCUMENT), equalTo(1));
    }

    @Test
    void repeatedDownloadCountsAreServedFromCacheRatherThanTheDatabase() {
        //given
        insert("download", TEST_DOCUMENT, 2);
        assertThat(service.totalDownloads(TEST_DOCUMENT), equalTo(2));

        //when
        insert("download", TEST_DOCUMENT, 40);

        //then
        assertThat(service.totalDownloads(TEST_DOCUMENT), equalTo(2));
    }

    /**
     * {@code totalViews} and {@code totalDownloads} take the same single {@code String} argument, so a
     * shared cache under the default key generator would give them the same key and one would serve the
     * other's number. They must be cached separately.
     */
    @Test
    void viewAndDownloadTotalsDoNotShareCacheEntries() {
        //given the same document has different view and download counts
        insert("view", TEST_DOCUMENT, 7);
        insert("download", TEST_DOCUMENT, 3);

        //when both are read, in either order
        val views = service.totalViews(TEST_DOCUMENT);
        val downloads = service.totalDownloads(TEST_DOCUMENT);

        //then neither has been served the other's total
        assertThat(views, equalTo(7));
        assertThat(downloads, equalTo(3));
    }

    /**
     * An unavailable count is {@code null} by design, so the record page renders without a counter.
     * Caching that null would turn a momentary database failure into a counter that stays missing for
     * the whole TTL, so the absence must not be stored.
     */
    @Test
    void unavailableTotalsAreNotCached() {
        //given the database cannot be reached
        insert("view", TEST_DOCUMENT, 5);
        dataSource.sever();

        //when the counts come back absent
        assertThat(service.totalViews(TEST_DOCUMENT), is(nullValue()));
        assertThat(service.totalDownloads(TEST_DOCUMENT), is(nullValue()));

        //then nothing was cached
        assertThat(cacheManager.getCache(VIEW_TOTALS_CACHE).get(TEST_DOCUMENT), is(nullValue()));
        assertThat(cacheManager.getCache(DOWNLOAD_TOTALS_CACHE).get(TEST_DOCUMENT), is(nullValue()));

        //and once the database is back, the next request recovers
        dataSource.restore();
        assertThat(service.totalViews(TEST_DOCUMENT), equalTo(5));
    }

    private void insert(String metricType, String document, int amount) {
        val id = UUID.fromString(document);
        jdbc.update("INSERT INTO metrics.documents (document_id, doc_title, record_type) VALUES (?, 'title', 'dataset') ON CONFLICT DO NOTHING", id);
        jdbc.update("INSERT INTO metrics.metric_counts (document_id, metric_type, counted_from, counted_until, amount) VALUES (?, ?, ?, ?, ?)",
            id, metricType, FROM, FROM.plusDays(1), amount);
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

    @Configuration
    @EnableCaching
    static class Config {
        /** Built lazily by Spring, i.e. after {@link #startPostgres()} has set the delegate. */
        @Bean
        SeverableDataSource dataSource() {
            return new SeverableDataSource(postgresDataSource);
        }

        @Bean
        DocumentRepository documentRepository() {
            return mock(DocumentRepository.class);
        }

        @Bean
        MetricsService metricsService(SeverableDataSource dataSource, DocumentRepository documentRepository) {
            val service = new JDBCMetricsService(dataSource, documentRepository);
            assertThat(service.ensureSchema(), is(true));
            return service;
        }

        @Bean
        CacheManager cacheManager() {
            val cacheManager = new CaffeineCacheManager();
            val spec = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).maximumSize(100);
            cacheManager.registerCustomCache(VIEW_TOTALS_CACHE, spec.build());
            cacheManager.registerCustomCache(DOWNLOAD_TOTALS_CACHE, spec.build());
            return cacheManager;
        }
    }

    /** Lets a test cut the service off from the database without touching the shared PostgreSQL. */
    static class SeverableDataSource extends AbstractDataSource {
        private final DataSource delegate;
        private volatile boolean severed;

        SeverableDataSource(DataSource delegate) {
            this.delegate = delegate;
        }

        void sever() {
            severed = true;
        }

        void restore() {
            severed = false;
        }

        @Override
        public Connection getConnection() throws SQLException {
            if (severed) {
                throw new SQLException("Connection to metrics database refused");
            }
            return delegate.getConnection();
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            if (severed) {
                throw new SQLException("Connection to metrics database refused");
            }
            return delegate.getConnection(username, password);
        }
    }
}
