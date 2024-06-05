package uk.ac.ceh.gateway.catalogue.services;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class JDBCMetricsServiceTest {
    private EmbeddedDatabase db;
    private JDBCMetricsService service;

    private static final String TEST_DOCUMENT = "123e4567-e89b-12d3-a456-426614174000";
    private static final String TEST_IP1 = "192.0.2.1";
    private static final String TEST_IP2 = "192.0.2.2";

    @BeforeEach
    void setup() {
        db = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .generateUniqueName(true)
            .build();
        service = new JDBCMetricsService(db);
    }

    @AfterEach
    void teardown() {
        db.shutdown();
    }

    @Test
    void testCreatedTables() throws SQLException {
        //given

        //when

        //then
        val rs = db.getConnection().getMetaData().getTables(null, null, "%", null);
        val tables = new ArrayList<String>();
        while (rs.next()) {
            tables.add(rs.getString(3));
        }

        assertThat(tables, hasItems(equalToIgnoringCase("views"), equalToIgnoringCase("downloads")));
    }

    @Test
    void testRecordView() throws SQLException {
        //given

        //when
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.recordView(TEST_DOCUMENT, TEST_IP2);
        service.syncDB();

        //then
        val rows = getDocumentsAndAmounts("views");
        assertThat(rows, contains(contains(TEST_DOCUMENT, 2)));
    }

    @Test
    void testRecordDownload() throws SQLException {
        //given

        //when
        service.recordDownload(TEST_DOCUMENT, TEST_IP1);
        service.recordDownload(TEST_DOCUMENT, TEST_IP1);
        service.recordDownload(TEST_DOCUMENT, TEST_IP2);
        service.syncDB();

        //then
        val rows = getDocumentsAndAmounts("downloads");
        assertThat(rows, contains(contains(TEST_DOCUMENT, 2)));
    }

    List<List<Object>> getDocumentsAndAmounts(String table) throws SQLException {
        val stmt = db.getConnection().createStatement();
        val rs = stmt.executeQuery("SELECT document, amount FROM " + table);
        val rows = new ArrayList<List<Object>>();
        while (rs.next()) {
            rows.add(List.of(rs.getString(1), rs.getInt(2)));
        }
        return rows;
    }
}
