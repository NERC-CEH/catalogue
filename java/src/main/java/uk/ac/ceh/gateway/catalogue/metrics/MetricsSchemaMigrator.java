package uk.ac.ceh.gateway.catalogue.metrics;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.jspecify.annotations.NonNull;

import javax.sql.DataSource;

/**
 * Owns the metrics schema: applies the Flyway migrations for the configured engine to the metrics
 * {@link DataSource}. {@link JDBCMetricsService} runs no DDL of its own; it calls {@link #migrate()} from
 * {@code ensureSchema}.
 *
 * <p><b>Why not Spring Boot's Flyway auto-configuration.</b> It is deliberately not on the classpath
 * ({@code spring-boot-flyway} is not a dependency), for two reasons:</p>
 * <ul>
 *   <li><em>Startup-failure policy.</em> The auto-configuration migrates while the context starts, and a
 *   failed migration fails the context. The catalogue must start while the metrics database is down
 *   (see {@code application-metrics.properties}), and must pick the schema up once it returns, without a
 *   restart. Running the migration from {@code ensureSchema} keeps both: an unreachable database is
 *   logged and retried on next use, exactly as the hand-written DDL was.</li>
 *   <li><em>Gating.</em> The auto-configuration migrates whatever {@code DataSource} it finds, in every
 *   profile. This class is only ever constructed by {@code MetricsDatabaseConfig}, which is
 *   {@code @Profile("metrics")}, so contexts without that profile never attempt a migration — no
 *   {@code spring.flyway.enabled} switch to forget.</li>
 * </ul>
 *
 * <p><b>Locations.</b> One per engine, because the PostgreSQL scripts cannot be applied to SQLite:
 * {@link #POSTGRESQL_LOCATION} (also run on H2 by the unit tests) and {@link #SQLITE_LOCATION} for the
 * rollback path. {@code failOnMissingLocations} is on so that a mistyped location fails loudly instead of
 * "successfully" migrating nothing and leaving the service believing the schema exists.</p>
 *
 * <p><b>Baseline version, per engine.</b> {@code baselineOnMigrate} only applies to a database that
 * already holds tables but has no {@code flyway_schema_history}; an empty database is never baselined
 * and simply gets {@code V1}. For a non-empty one:</p>
 * <ul>
 *   <li><em>SQLite: baseline 1.</em> The production file already has the V1 schema (both tables and
 *   #242's covering indexes), so it is recorded as being at V1 and {@code V1} is skipped — no SQL runs
 *   against the 1.1 GB file on the CIFS share.</li>
 *   <li><em>PostgreSQL: baseline 0.</em> A PostgreSQL that ran the pre-Flyway service (#234) has the
 *   tables but not the primary key or the report indexes, so {@code V1} still runs there; it is written
 *   to be a no-op for anything that already exists and only add what is missing.</li>
 * </ul>
 * <p>Flyway does not verify that a baselined schema actually matches {@code V1}; the SQLite baseline
 * trusts the production file to have the shape #242 left it in.</p>
 *
 * <p>Requires {@code org.flywaydb:flyway-database-postgresql} on PostgreSQL (a separate artifact since
 * Flyway 10 — without it Flyway fails with "Unsupported Database"). H2 and SQLite support is in
 * {@code flyway-core}.</p>
 */
@Slf4j
public class MetricsSchemaMigrator {
    public static final String POSTGRESQL_LOCATION = "classpath:db/metrics/postgresql";
    public static final String SQLITE_LOCATION = "classpath:db/metrics/sqlite";

    /** A pre-existing schema is baselined below V1, so V1 still runs on it (PostgreSQL). */
    public static final String POSTGRESQL_BASELINE_VERSION = "0";
    /** A pre-existing schema is taken to be V1 already, so V1 is skipped on it (SQLite rollback path). */
    public static final String SQLITE_BASELINE_VERSION = "1";

    private final DataSource dataSource;
    @Getter private final String location;
    @Getter private final String baselineVersion;

    /** Baseline {@value #POSTGRESQL_BASELINE_VERSION}: a pre-existing schema still gets V1. */
    public MetricsSchemaMigrator(@NonNull DataSource dataSource, @NonNull String location) {
        this(dataSource, location, POSTGRESQL_BASELINE_VERSION);
    }

    /**
     * Does no I/O: Flyway is configured and connects only in {@link #migrate()}.
     *
     * @param baselineVersion the version a non-empty database without Flyway history is recorded at;
     *        {@code "1"} marks an existing schema as already being V1, so V1 is skipped there
     */
    public MetricsSchemaMigrator(@NonNull DataSource dataSource, @NonNull String location,
                                 @NonNull String baselineVersion) {
        this.dataSource = dataSource;
        this.location = location;
        this.baselineVersion = baselineVersion;
    }

    /**
     * Brings the schema up to date. Throws (a {@code FlywayException}, a {@code RuntimeException}) if the
     * database cannot be reached or a migration fails; the caller decides what that means.
     *
     * <p>A fresh {@link Flyway} per call: this runs once on a healthy database, and only repeats while
     * the database is unavailable, so there is nothing worth caching. {@code connectRetries(0)} makes a
     * single connection attempt, so a call against a database that is down costs one connection timeout,
     * not several.</p>
     *
     * <p>Concurrent pods are safe on PostgreSQL, which Flyway serialises with an advisory lock. SQLite has
     * no such lock, which is acceptable for the rollback path: one pod, and its {@code V1} only runs on an
     * empty file and is idempotent.</p>
     */
    public void migrate() {
        var result = Flyway.configure()
            .dataSource(dataSource)
            .locations(location)
            .failOnMissingLocations(true)
            .baselineOnMigrate(true)
            .baselineVersion(baselineVersion)
            .baselineDescription("Pre-Flyway metrics schema")
            .connectRetries(0)
            .load()
            .migrate();
        log.info("Metrics schema at version {} from {} ({} migration(s) applied)",
            result.targetSchemaVersion, location, result.migrationsExecuted);
    }
}
