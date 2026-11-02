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
            log.info("Connecting to PostgreSQL metrics database: {}", properties.getUrl());
            return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        }
    }
}
