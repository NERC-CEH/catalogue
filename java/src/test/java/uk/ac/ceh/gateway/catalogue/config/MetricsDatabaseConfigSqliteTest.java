package uk.ac.ceh.gateway.catalogue.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.NestedTestConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.sqlite.SQLiteDataSource;
import uk.ac.ceh.gateway.catalogue.metrics.MetricsSchemaMigrator;

import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * The SQLite branch of {@link MetricsDatabaseConfig} — the current production behaviour, and the
 * rollback target for the PostgreSQL migration (#236).
 *
 * <p>No {@code DataSourceProperties} fixture: {@code PostgresMetricsDatabase} is conditioned off here,
 * so nothing consumes one, and a PostgreSQL URL sitting in a SQLite test only suggests a relationship
 * that does not exist.
 *
 * <p>The database path is relative and under {@code build/}, matching
 * {@code uk/ac/ceh/gateway/catalogue/test.properties}. Nothing here opens a connection — the beans only
 * configure the driver and Flyway — so no file is created and none needs cleaning up.
 */
@SpringBootTest(classes = MetricsDatabaseConfig.class)
@ActiveProfiles("metrics")
@TestPropertySource(properties = {
    "metrics.database.engine=sqlite",
    "metrics.database.url=jdbc:sqlite:build/metrics-sqlite-config-test.db",
    "metrics.database.busy-timeout-millis=5000"
})
class MetricsDatabaseConfigSqliteTest {

    @Autowired private DataSource dataSource;
    @Autowired private MetricsSchemaMigrator schemaMigrator;

    /**
     * The rollback path gets its own migrations: the PostgreSQL ones cannot be applied to SQLite, and
     * without any, removing the constructor DDL would leave a fresh SQLite database with no schema. Its
     * baseline is 1, so the existing production file is recorded as V1 and V1 is skipped there.
     */
    @Test
    void sqliteEngineMigratesWithTheSqliteScripts() {
        assertThat(schemaMigrator.getLocation(), is(MetricsSchemaMigrator.SQLITE_LOCATION));
        assertThat(schemaMigrator.getBaselineVersion(), is(MetricsSchemaMigrator.SQLITE_BASELINE_VERSION));
    }

    @Test
    void sqliteEngineCreatesSQLiteDataSource() {
        //given/when the metrics profile is active with the sqlite engine

        //then the datasource is SQLite, pointed at the configured file
        assertThat(dataSource, instanceOf(SQLiteDataSource.class));
        assertThat(((SQLiteDataSource) dataSource).getUrl(), is("jdbc:sqlite:build/metrics-sqlite-config-test.db"));
    }

    @Test
    void postgresPoolIsNotActivatedAlongsideSqlite() {
        //given/when the engine is sqlite

        //then the PostgreSQL branch contributed nothing
        assertThat(dataSource, is(not(instanceOf(HikariDataSource.class))));
    }

    /**
     * The busy timeout is the entire reason this bean is built by hand rather than from a URL alone.
     * SQLite's default is 0, so a reader that meets the hourly {@code JDBCMetricsService.syncDB} writer's
     * lock fails immediately with {@code SQLITE_BUSY} instead of waiting — and those reads happen while a
     * record page renders. ({@code journal_mode=WAL} would be the usual answer, but it relies on shared
     * memory and is unsafe on the network filesystem this database lives on.)
     *
     * <p>Asserted because the URL assertion above would not notice its removal: drop the
     * {@code SQLiteConfig} and the datasource still builds, still carries the right URL, and quietly
     * reinstates the failure mode the config exists to prevent.</p>
     */
    @Test
    void busyTimeoutIsAppliedSoReadsWaitForTheHourlySyncRatherThanFailing() {
        //given/when the SQLite datasource is built
        SQLiteDataSource sqliteDataSource = (SQLiteDataSource) dataSource;

        //then the driver will wait rather than return SQLITE_BUSY
        assertThat(sqliteDataSource.getConfig().toProperties().getProperty("busy_timeout"), is("5000"));
    }

    /**
     * {@code matchIfMissing = true} is the rollback guarantee, and it is load-bearing:
     * {@code metrics.database.engine} is set in neither {@code application.properties} nor
     * {@code application-metrics.properties}, so every environment that has not explicitly opted in to
     * PostgreSQL reaches SQLite through this default alone. Removing a ConfigMap entry has to be enough
     * to roll back, which means the absent case needs its own context rather than being implied by the
     * explicit {@code engine=sqlite} above.
     *
     * <p>{@code OVERRIDE} stops the enclosing class's {@code engine=sqlite} being inherited — inheriting
     * it would set the very property this test needs absent.</p>
     */
    @Nested
    @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
    @SpringBootTest(classes = MetricsDatabaseConfig.class)
    @ActiveProfiles("metrics")
    @TestPropertySource(properties = {
        "metrics.database.url=jdbc:sqlite:build/metrics-sqlite-default-test.db",
        "metrics.database.busy-timeout-millis=5000"
    })
    class EngineUnset {

        @Autowired private DataSource defaultDataSource;

        @Test
        void sqliteIsTheEngineWhenNoneIsNamed() {
            //given/when metrics.database.engine is not set anywhere

            //then the environment keeps the behaviour it has today
            assertThat(defaultDataSource, instanceOf(SQLiteDataSource.class));
            assertThat(defaultDataSource, is(not(instanceOf(HikariDataSource.class))));
        }
    }
}
