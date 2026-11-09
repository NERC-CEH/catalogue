package uk.ac.ceh.gateway.catalogue.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;
import uk.ac.ceh.gateway.catalogue.CatalogueWebTest;
import uk.ac.ceh.gateway.catalogue.metrics.JDBCMetricsService;
import uk.ac.ceh.gateway.catalogue.metrics.MetricsService;
import uk.ac.ceh.gateway.catalogue.metrics.SqliteMetricsService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The EIDC production context with {@code metrics.database.engine=postgres}.
 *
 * <p>{@link EidcApplicationContextTest} gets {@link SqliteMetricsService} through {@code matchIfMissing},
 * and {@link MetricsDatabaseConfigPostgresTest} loads only the datasource config. This is the one test
 * that builds the postgres {@code @ConditionalOnProperty} wiring, the {@code @Value} injection, Flyway's
 * {@code classpath:db/metrics} lookup and the {@code @Cacheable} proxy together in a real Spring
 * context, then migrates and reads through them.</p>
 *
 * <p>Database: {@code METRICS_TEST_POSTGRES_URL} (with optional {@code _USER}/{@code _PASSWORD}) in CI,
 * otherwise a Testcontainers PostgreSQL; skipped locally when Docker is unavailable.</p>
 */
@ActiveProfiles({"auth-crowd", "upload-hubbub", "server-eidc", "search-basic", "service-agreement", "metrics"})
@CatalogueWebTest
@DisplayName("EIDC production context, postgres metrics engine")
class EidcPostgresMetricsContextTest {
    private static final String IMAGE = "postgres:14.24-alpine";
    private static final String TEST_DOCUMENT = "123e4567-e89b-12d3-a456-426614174000";

    private static PostgreSQLContainer postgres;
    private static String url;
    private static String username;
    private static String password;

    @Autowired private ApplicationContext applicationContext;
    @Autowired private MetricsService metricsService;
    @Autowired private DataSource dataSource;
    @Autowired private CacheManager cacheManager;

    @BeforeAll
    static void startPostgres() {
        val externalUrl = System.getenv("METRICS_TEST_POSTGRES_URL");
        if (externalUrl != null) {
            url = externalUrl;
            username = envOr("METRICS_TEST_POSTGRES_USER", "catalogue");
            password = envOr("METRICS_TEST_POSTGRES_PASSWORD", "catalogue");
            awaitConnection(new DriverManagerDataSource(url, username, password), url);
        } else {
            assertThat("METRICS_TEST_POSTGRES_URL must be set in CI", System.getenv("CI"), is(nullValue()));
            assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is not available");
            postgres = new PostgreSQLContainer(IMAGE)
                .withDatabaseName("catalogue")
                .withUsername("catalogue")
                .withPassword("catalogue");
            postgres.start();
            url = postgres.getJdbcUrl();
            username = postgres.getUsername();
            password = postgres.getPassword();
        }
    }

    @AfterAll
    static void stopPostgres() {
        if (postgres != null) {
            postgres.stop();
        }
    }

    /** Through the real CATALOGUE_DATASOURCE_* placeholders, as a deployment would set them. */
    @DynamicPropertySource
    static void postgresEngine(DynamicPropertyRegistry registry) {
        registry.add("metrics.database.engine", () -> "postgres");
        registry.add("CATALOGUE_DATASOURCE_URL", () -> url);
        registry.add("CATALOGUE_DATASOURCE_USERNAME", () -> username);
        registry.add("CATALOGUE_DATASOURCE_PASSWORD", () -> password);
    }

    @Test
    @DisplayName("The postgres engine builds JDBCMetricsService, not SqliteMetricsService")
    void postgresServiceIsSelected() {
        assertThat(AopUtils.getTargetClass(metricsService), is(equalTo(JDBCMetricsService.class)));
        assertThat(applicationContext.getBeanNamesForType(SqliteMetricsService.class), is(emptyArray()));
        assertThat(dataSource, instanceOf(HikariDataSource.class));
        assertThat(((HikariDataSource) dataSource).getJdbcUrl(), is(url));
    }

    @Test
    @DisplayName("@Value properties are injected into the postgres service")
    void valuePropertiesAreInjected() {
        assertThat(ReflectionTestUtils.getField(target(), "shutdownFlushTimeoutMillis"), is(8000L));
    }

    @Test
    @DisplayName("Flyway finds classpath:db/metrics and migrates the metrics schema")
    void flywayMigratesFromTheClasspath() {
        assertThat(target().ensureSchema(), is(true));
        assertThat(new JdbcTemplate(dataSource).queryForList(
                "SELECT version FROM metrics.flyway_schema_history WHERE success AND type = 'SQL'", String.class),
            hasItem("1"));
    }

    @Test
    @DisplayName("Count reads go through the caching proxy and hit the database")
    void countReadsAreCachedThroughTheProxy() {
        assertThat(AopUtils.isAopProxy(metricsService), is(true));
        cacheManager.getCache(JDBCMetricsService.VIEW_TOTALS_CACHE).clear();

        assertThat(metricsService.totalViews(TEST_DOCUMENT), is(0));

        assertThat(cacheManager.getCache(JDBCMetricsService.VIEW_TOTALS_CACHE).get(TEST_DOCUMENT), is(notNullValue()));
    }

    private JDBCMetricsService target() {
        return AopTestUtils.getUltimateTargetObject(metricsService);
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
}
