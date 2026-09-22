package uk.ac.ceh.gateway.catalogue.config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@Slf4j
@Profile("metrics")
public class MetricsDatabaseConfig {
    @Value("${metrics.database.url}") private String databaseUrl;
    @Value("${metrics.database.busy-timeout-millis}") private int busyTimeoutMillis;

    /**
     * SQLite's default busy timeout is 0, so a reader that meets a writer's lock fails immediately
     * with {@code SQLITE_BUSY} rather than waiting. The hourly {@code JDBCMetricsService.syncDB}
     * writes while record pages are reading view/download counts, so without a timeout those reads
     * fail whenever the two overlap.
     *
     * <p>Note {@code journal_mode=WAL} would be the usual way to stop writers blocking readers, but
     * it relies on shared memory and is unsafe on the network filesystem this database lives on.</p>
     */
    @Bean
    public DataSource dataSource() {
        log.info("Connecting to SQLite Database: {} (busy timeout {}ms)", databaseUrl, busyTimeoutMillis);
        SQLiteConfig config = new SQLiteConfig();
        config.setBusyTimeout(busyTimeoutMillis);
        SQLiteDataSource dataSource = new SQLiteDataSource(config);
        dataSource.setUrl(databaseUrl);
        return dataSource;
    }
}
