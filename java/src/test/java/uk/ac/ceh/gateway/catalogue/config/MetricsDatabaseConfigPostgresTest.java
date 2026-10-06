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
 *
 * <p>No test fixture supplies {@code DataSourceProperties}. Binding {@code spring.datasource.*} onto
 * that bean is the behaviour under test — it is the most likely thing to break, since the pool is
 * hand-built here rather than auto-configured — and a {@code @Primary} stand-in holding hard-coded
 * connection details would satisfy every assertion below without the binding ever running. The
 * assertions therefore read the settings back off the built pool.
 *
 * <p>Only the engine and the connection details are set here. The pool settings deliberately come from
 * the shipped {@code application-metrics.properties}, which the {@code metrics} profile loads: setting
 * them here as well would outrank that file, and the assertions would only be reading back this
 * annotation, passing even with a value deleted from the file.</p>
 */
@SpringBootTest(classes = {
    MetricsDatabaseConfigPostgresTest.EnableBinding.class,
    MetricsDatabaseConfig.class
})
@ActiveProfiles("metrics")
@TestPropertySource(properties = {
    "metrics.database.engine=postgres",
    "spring.datasource.url=jdbc:postgresql://localhost:5432/metrics",
    "spring.datasource.username=metrics",
    "spring.datasource.password=metrics"
})
class MetricsDatabaseConfigPostgresTest {

    /**
     * Registers {@code ConfigurationPropertiesBindingPostProcessor}, which is what makes
     * {@code @ConfigurationProperties} on the config's {@code @Bean} methods do anything.
     *
     * <p>Naming a component class on {@code @SpringBootTest} makes it the primary source and runs no
     * auto-configuration, so {@code ConfigurationPropertiesAutoConfiguration} — which supplies that
     * post-processor in production — is absent. Without it the annotations are silently inert: the
     * context starts, {@code DataSourceProperties} stays empty, and {@code initializeDataSourceBuilder()}
     * finds no URL and quietly falls back to an embedded H2 because H2 is on the test classpath. That
     * fallback is the trap this class has to avoid, since it fails nothing by itself and would otherwise
     * be mistaken for a working PostgreSQL pool.
     *
     * <p>This contributes no beans and stubs no values — the connection and pool settings still come from
     * {@code @TestPropertySource} and {@code application-metrics.properties} through the same binding
     * production uses, which is the point of the assertions below.
     */
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

    /**
     * The SQLite branch must be off, not merely outvoted. Both branches declare a bean named
     * {@code dataSource}, so if the conditions ever stopped being mutually exclusive the context would
     * fail rather than pick one — but an engine value that matched neither would leave no datasource at
     * all, which is the failure this guards against alongside {@link #postgresEngineCreatesHikariDataSource}.
     */
    @Test
    void sqliteEngineIsNotActivatedAlongsidePostgres() {
        //given/when the engine is postgres

        //then the SQLite branch contributed nothing
        assertThat(dataSource, is(not(instanceOf(SQLiteDataSource.class))));
    }

    /**
     * Connection details come from {@code spring.datasource.*}, deliberately not from
     * {@code metrics.database.url}: the two engines read separate properties so that an environment can
     * carry both and switching is a one-value change. Asserting the URL and username off the built pool
     * is what proves the {@code @ConfigurationProperties("spring.datasource")} binding actually happened,
     * rather than the pool being handed details from somewhere else.
     */
    @Test
    void connectionDetailsAreBoundFromSpringDatasourceProperties() {
        //given/when the pool is built
        HikariDataSource pool = (HikariDataSource) dataSource;

        //then it is pointed at the configured database
        assertThat(pool.getJdbcUrl(), is("jdbc:postgresql://localhost:5432/metrics"));
        assertThat(pool.getUsername(), is("metrics"));
    }

    @Test
    void poolSizingIsBoundFromHikariProperties() {
        //given/when the pool is built
        HikariDataSource pool = (HikariDataSource) dataSource;

        //then the sizing from application-metrics.properties applies
        assertThat(pool.getPoolName(), is("metrics-pool"));
        assertThat(pool.getMaximumPoolSize(), is(8));
        assertThat(pool.getMinimumIdle(), is(2));
    }

    /**
     * The timeouts that keep a metrics outage off the record page. A render reads two counts, and
     * {@code JDBCMetricsService.totalAmount} returns null on any {@code DataAccessException} so the page
     * renders without them — but only once the read gives up. {@code connection-timeout} bounds the wait
     * for a pooled connection; the driver's {@code socketTimeout} bounds a query on a connection whose
     * peer has vanished without a reset, which pgjdbc otherwise waits on forever. Hikari's and pgjdbc's
     * defaults are 30s and unbounded, so these are asserted because nothing else in the suite would
     * notice their removal from the properties file.
     *
     * <p>{@code initialization-fail-timeout=-1} is asserted too, as the stated setting, although it is not
     * what keeps the catalogue starting with the database down: that is {@code JDBCMetricsService} running
     * no SQL while the context starts, covered by {@code JDBCMetricsServiceTest}.</p>
     */
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
