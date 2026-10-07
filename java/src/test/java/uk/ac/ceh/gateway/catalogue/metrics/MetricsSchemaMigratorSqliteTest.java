package uk.ac.ceh.gateway.catalogue.metrics;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlite.SQLiteDataSource;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;

/**
 * The SQLite rollback path (#234 keeps {@code metrics.database.engine=sqlite} selectable). Removing the
 * DDL from {@link JDBCMetricsService} left that path with no schema creation at all, and the PostgreSQL
 * migrations cannot be applied to SQLite, so it has its own: {@code db/metrics/sqlite}, with baseline
 * version 1. Both cases it has to handle are covered here, against real SQLite files.
 */
@ExtendWith(MockitoExtension.class)
class MetricsSchemaMigratorSqliteTest {
    private static final String TEST_DOCUMENT = "123e4567-e89b-12d3-a456-426614174000";

    @TempDir Path dir;
    @Mock private DocumentRepository documentRepository;

    /**
     * A fresh local or CI database: empty, so it is not baselined, and V1 creates the tables and #242's
     * covering indexes.
     */
    @SneakyThrows
    @Test
    void freshSqliteDatabaseIsCreatedByMigration() {
        //given an empty SQLite file, with the production SQLite configuration (baseline 1)
        val dataSource = sqlite("fresh.db");
        val service = new JDBCMetricsService(dataSource, documentRepository, sqliteMigrator(dataSource));

        //when
        assertThat(service.ensureSchema(), is(true));

        //then the schema exists, Flyway owns it, and the service can write and read through it
        val jdbc = new JdbcTemplate(dataSource);
        assertThat(jdbc.queryForList("SELECT name FROM sqlite_master WHERE type = 'index'", String.class),
            hasItems("idx_views_document_amount", "idx_downloads_document_amount"));
        assertThat(jdbc.queryForObject("SELECT max(version) FROM flyway_schema_history WHERE success = 1", String.class), is("1"));
        //and it was V1 that created it, not a baseline: an empty database is never baselined
        assertThat(jdbc.queryForObject("SELECT type FROM flyway_schema_history WHERE version = '1'", String.class), is("SQL"));
        assertThat(syncOneViewAndReadTotal(service), equalTo(1));
    }

    /**
     * The production file: tables and #242's indexes already there, no id column, no schema history. It
     * must be baselined at 1, so V1 is skipped, and left exactly as it is — rows intact, no rebuilds on a
     * 1.1 GB CIFS-hosted file — and the service must still write to it, which it can because its insert
     * names its columns.
     */
    @SneakyThrows
    @Test
    void existingProductionSqliteDatabaseIsBaselinedAndLeftIntact() {
        //given a file in the shape the old service constructor created, holding a row
        val dataSource = sqlite("production.db");
        val jdbc = new JdbcTemplate(dataSource);
        for (String table : List.of("views", "downloads")) {
            jdbc.execute(("CREATE TABLE %s (start_timestamp bigint NOT NULL, end_timestamp bigint NOT NULL, amount integer NOT NULL, "
                + "document text NOT NULL, doc_title text NOT NULL, record_type text NOT NULL)").formatted(table));
            jdbc.execute("CREATE INDEX idx_%1$s_document_amount ON %1$s (document, amount)".formatted(table));
        }
        jdbc.update("INSERT INTO views (start_timestamp, end_timestamp, amount, document, doc_title, record_type) VALUES (1, 2, 41, ?, 't', 'r')",
            TEST_DOCUMENT);
        val service = new JDBCMetricsService(dataSource, documentRepository, sqliteMigrator(dataSource));

        //when
        assertThat(service.ensureSchema(), is(true));

        //then the file was baselined at V1 and no migration script ran against it
        assertThat(jdbc.queryForObject("SELECT type FROM flyway_schema_history WHERE version = '1'", String.class), is("BASELINE"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM flyway_schema_history WHERE type = 'SQL'", Integer.class), is(0));
        //and the tables are unchanged, the existing row intact, and new counts are written
        assertThat(jdbc.queryForObject("SELECT count(*) FROM pragma_table_info('views') WHERE name = 'id'", Integer.class), is(0));
        assertThat(syncOneViewAndReadTotal(service), equalTo(42));
    }

    @SneakyThrows
    private Integer syncOneViewAndReadTotal(JDBCMetricsService service) {
        MetadataDocument doc = new GeminiDocument();
        doc.setTitle("title");
        doc.setType("dataset");
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        service.recordView(TEST_DOCUMENT, "192.0.2.1");
        service.syncDB();
        return service.totalViews(TEST_DOCUMENT);
    }

    /** As MetricsDatabaseConfig builds it for the sqlite engine. */
    private static MetricsSchemaMigrator sqliteMigrator(SQLiteDataSource dataSource) {
        return new MetricsSchemaMigrator(dataSource, MetricsSchemaMigrator.SQLITE_LOCATION,
            MetricsSchemaMigrator.SQLITE_BASELINE_VERSION);
    }

    private SQLiteDataSource sqlite(String file) {
        val dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + dir.resolve(file));
        return dataSource;
    }
}
