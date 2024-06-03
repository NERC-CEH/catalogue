package uk.ac.ceh.gateway.catalogue.config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.sqlite.SQLiteDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@Slf4j
@Profile("metrics")
public class MetricsDatabaseConfig {
    @Value("${metrics.database.url}") private String databaseUrl;

    @Bean
    public DataSource dataSource() {
        log.info("Connecting to SQLite Database: {}", databaseUrl);
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(databaseUrl);
        return dataSource;
    }
}
