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
 * The metrics {@link DataSource}, selected by {@code metrics.database.engine} ({@code sqlite}, the
 * default, or {@code postgres}).
 *
 * <p><strong>Why the PostgreSQL pool is built by hand.</strong> {@code DataSourceAutoConfiguration} is
 * excluded globally on {@code CatalogueApplication}. Left enabled, every context without the
 * {@code metrics} profile (DataLabs, NonEidc, most tests) would have the PostgreSQL driver on the
 * classpath, no {@code spring.datasource.url} and no embedded database, and would fail to start with
 * "Failed to determine a suitable driver class".</p>
 *
 * <p><strong>Why the exclusion cannot be undone here.</strong> An application-wide exclude wins over a
 * profile-scoped re-import: {@code @ImportAutoConfiguration(DataSourceAutoConfiguration.class)} on a
 * {@code @Profile("metrics")} class is filtered out by the same exclusion and silently contributes
 * nothing. So the postgres branch builds the Hikari pool itself, through the same
 * {@link DataSourceProperties} and {@code DataSourceBuilder} the auto-configuration would use, which is
 * why {@code spring.datasource.*} and {@code spring.datasource.hikari.*} still bind as normal (see
 * {@code application-metrics.properties}).</p>
 *
 * <p>No SQL runs here: the pool is lazy, and the schema is migrated by {@code JDBCMetricsService} after
 * startup, so an unreachable database never stops the catalogue starting. A <em>missing</em>
 * connection URL is different: that is a deployment mistake, not an outage, so it fails startup.</p>
 */
@Configuration
@Slf4j
@Profile("metrics")
public class MetricsDatabaseConfig {

    /**
     * SQLite: the default engine, and the current production behaviour.
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
     */
    @ConditionalOnProperty(name = "metrics.database.engine", havingValue = "postgres")
    static class PostgresMetricsDatabase {

        @Bean
        @ConfigurationProperties("spring.datasource")
        public DataSourceProperties dataSourceProperties() {
            return new DataSourceProperties();
        }

        @Bean
        @ConfigurationProperties("spring.datasource.hikari")
        public DataSource dataSource(DataSourceProperties properties) {
            requireUrl(properties.getUrl());
            log.info("Connecting to PostgreSQL metrics database: {}", properties.getUrl());
            return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        }

        /**
         * Fail loudly on a missing CATALOGUE_DATASOURCE_URL. Without this, an environment still setting
         * the old variable names gets an unresolved placeholder as its URL, and JDBCMetricsService would
         * only log a WARN and retry forever.
         */
        static void requireUrl(String url) {
            if (url == null || url.isBlank() || url.contains("${")) {
                throw new IllegalStateException("metrics.database.engine=postgres but spring.datasource.url is not set; "
                    + "set CATALOGUE_DATASOURCE_URL (and CATALOGUE_DATASOURCE_USERNAME/_PASSWORD)");
            }
        }
    }
}
