package uk.ac.ceh.gateway.catalogue.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

/**
 * The PostgreSQL branch of {@link MetricsDatabaseConfig}.
 */
@SpringBootTest(classes = {
    MetricsDatabaseConfigPostgresTest.EnableBinding.class,
    MetricsDatabaseConfig.class
})
@ActiveProfiles("metrics")
@TestPropertySource(properties = {
    "metrics.database.engine=postgres",
    "spring.datasource.url=jdbc:postgresql://localhost:5432/catalogue",
    "spring.datasource.username=catalogue",
    "spring.datasource.password=catalogue"
})
class MetricsDatabaseConfigPostgresTest {
    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties
    static class EnableBinding {
    }

    @Autowired private DataSource dataSource;

    @Test
    void postgresEngineCreatesHikariDataSource() {
        //given/when the metrics profile is active with the postgres engine

        //then the datasource is a HikariCP pool, and not an embedded fallback
        assertThat(dataSource, instanceOf(HikariDataSource.class));
        assertThat(((HikariDataSource) dataSource).getJdbcUrl(), startsWith("jdbc:postgresql:"));
    }

    @Test
    void sqliteEngineIsNotActivatedAlongsidePostgres() {
        //given/when the engine is postgres

        //then the SQLite branch contributed nothing
        assertThat(dataSource, is(not(instanceOf(SQLiteDataSource.class))));
    }

    @Test
    void connectionDetailsAreBoundFromSpringDatasourceProperties() {
        //given/when the pool is built
        HikariDataSource pool = (HikariDataSource) dataSource;

        //then it is pointed at the configured database
        assertThat(pool.getJdbcUrl(), is("jdbc:postgresql://localhost:5432/catalogue"));
        assertThat(pool.getUsername(), is("catalogue"));
    }

    @Test
    void poolSizingIsBoundFromHikariProperties() {
        //given/when the pool is built
        HikariDataSource pool = (HikariDataSource) dataSource;

        //then the sizing from application-metrics.properties applies
        assertThat(pool.getPoolName(), is("catalogue-pool"));
        assertThat(pool.getMaximumPoolSize(), is(8));
        assertThat(pool.getMinimumIdle(), is(2));
    }

    @Test
    void timeoutsLetARecordPageGiveUpOnAnUnavailableDatabase() {
        //given/when the pool is built
        HikariDataSource pool = (HikariDataSource) dataSource;

        //then reads give up quickly, whether waiting for a connection or on a dead one
        assertThat(pool.getConnectionTimeout(), is(2000L));
        assertThat(pool.getDataSourceProperties().getProperty("socketTimeout"), is("10"));
        assertThat(pool.getInitializationFailTimeout(), is(-1L));
    }
}
