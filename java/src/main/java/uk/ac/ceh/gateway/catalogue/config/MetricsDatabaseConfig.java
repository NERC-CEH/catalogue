package uk.ac.ceh.gateway.catalogue.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;

/**
 * The metrics {@link DataSource}, which has exactly one consumer
 * ({@code JDBCMetricsService}) — so the blast radius of introducing a pool is that one class.
 *
 * <p><b>Engine selection.</b> {@code metrics.database.engine} chooses the implementation:
 * {@code sqlite} (the default) or {@code postgres}. A property rather than a sub-profile because all
 * four metrics beans — this class, {@code JDBCMetricsService}, {@code MetricsService} and
 * {@code MetricsReportController} — are {@code @Profile("metrics")}, so a sub-profile scheme would
 * have to be invented and threaded through the deployment's {@code SPRING_PROFILES_ACTIVE}.
 *
 * <p>SQLite is the default deliberately, for the duration of the migration: an environment that is
 * not explicitly opted in keeps the behaviour it has today, and a rollback is the removal of one
 * ConfigMap entry rather than the addition of one. The PostgreSQL environments name the engine
 * explicitly. Once the migration (#236) is verified in production and the rollback window closes, the
 * default flips to {@code postgres}, that branch and {@code metrics.database.busy-timeout-millis} go,
 * and the {@code sqlite-jdbc} dependency goes with them.
 *
 * <p><b>Why the pool is declared here rather than auto-configured.</b> The project depends on
 * {@code spring-boot-starter-jdbc}, which supplies HikariCP and puts
 * {@code DataSourceAutoConfiguration} on the auto-configuration candidate list for <em>every</em>
 * profile. The metrics datasource is {@code @Profile("metrics")}, so in contexts without that profile
 * (DataLabs, NonEidc, most tests) the auto-configuration would find the PostgreSQL driver on the
 * classpath, no {@code spring.datasource.url} and no embedded database to fall back on, and fail at
 * startup with "Failed to determine a suitable driver class". It is therefore excluded on
 * {@code CatalogueApplication}.
 *
 * <p>That exclusion is global and cannot be undone for one profile — re-importing the same class with
 * {@code @ImportAutoConfiguration} inside this class does <em>not</em> bring it back, which shows up
 * as "No qualifying bean of type 'javax.sql.DataSource'" when the metrics profile is active. So the
 * PostgreSQL branch below builds the pool itself. It still binds {@code spring.datasource.*} and
 * {@code spring.datasource.hikari.*}, through the same {@link DataSourceProperties} and
 * {@code DataSourceBuilder} the auto-configuration uses, so the property names and
 * {@code application-metrics.properties} are exactly as they would be if it were auto-configured.
 * Flyway (#235) will need the same treatment.
 */
@Configuration
@Slf4j
@Profile("metrics")
public class MetricsDatabaseConfig {

    /**
     * SQLite: the default engine, and the current production behaviour.
     *
     * <p>SQLite's default busy timeout is 0, so a reader that meets a writer's lock fails immediately
     * with {@code SQLITE_BUSY} rather than waiting. The hourly {@code JDBCMetricsService.syncDB}
     * writes while record pages are reading view/download counts, so without a timeout those reads
     * fail whenever the two overlap.
     *
     * <p>Note {@code journal_mode=WAL} would be the usual way to stop writers blocking readers, but
     * it relies on shared memory and is unsafe on the network filesystem this database lives on.
     *
     * <p>Unpooled, as before: this path has to behave exactly as production does today, and pooling
     * SQLite on a {@code nobrl} CIFS share is not a change worth making to something that is going to
     * be deleted. One consequence to keep in mind is that every statement opens its own connection,
     * which is why the production-context test points at a file-backed database rather than
     * {@code :memory:}.
     *
     * <p>Note the driver does not create the parent directory of the database file. Locally that
     * directory is inside the bind-mounted project tree, so it has to exist in the working copy.
     */
    @ConditionalOnProperty(name = "metrics.database.engine", havingValue = "sqlite", matchIfMissing = true)
    static class SqliteMetricsDatabase {

        @Bean
        public DataSource dataSource(
            @Value("${metrics.database.url}") String url,
            @Value("${metrics.database.busy-timeout-millis}") int busyTimeoutMillis
        ) {
            log.info("Connecting to SQLite Database: {} (busy timeout {}ms)", url, busyTimeoutMillis);
            SQLiteConfig config = new SQLiteConfig();
            config.setBusyTimeout(busyTimeoutMillis);
            SQLiteDataSource dataSource = new SQLiteDataSource(config);
            dataSource.setUrl(url);
            return dataSource;
        }
    }

    /**
     * PostgreSQL, pooled with HikariCP.
     *
     * <p>Reads {@code spring.datasource.*}, NOT {@code metrics.database.url} — the two engines take
     * their connection details from separate properties on purpose, so that an environment can carry
     * both and switching engines is genuinely a one-value change. Sharing one property would mean a
     * rollback handed a PostgreSQL URL to the SQLite driver.
     *
     * <p>Pool settings are bound from {@code spring.datasource.hikari.*} rather than set in code, so
     * they stay alongside the connection settings in {@code application-metrics.properties} and can be
     * tuned per environment without a rebuild: the pool size, the deliberately short
     * {@code connection-timeout} and the driver's {@code socketTimeout}; the reasoning for each is
     * recorded next to the values. The startup-failure policy (the catalogue starts when the metrics
     * database is down) is noted there too, but is enforced by {@code JDBCMetricsService} doing no SQL
     * while the context starts, not by any pool setting.
     */
    @ConditionalOnProperty(name = "metrics.database.engine", havingValue = "postgres")
    static class PostgresMetricsDatabase {

        @Bean
        @ConfigurationProperties("spring.datasource")
        public DataSourceProperties dataSourceProperties() {
            return new DataSourceProperties();
        }

        /**
         * {@code destroyMethod} is left to Hikari's own {@code close()}, which Spring finds by
         * convention, so the pool is shut down with the context rather than leaking its threads.
         */
        @Bean
        @ConfigurationProperties("spring.datasource.hikari")
        public DataSource dataSource(DataSourceProperties properties) {
            log.info("Connecting to PostgreSQL metrics database: {}", properties.getUrl());
            return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        }
    }
}
