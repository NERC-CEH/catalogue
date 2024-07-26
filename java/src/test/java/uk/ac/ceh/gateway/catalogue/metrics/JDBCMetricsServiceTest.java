package uk.ac.ceh.gateway.catalogue.metrics;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.IntStream;

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

    @Test
    void testCountViews() throws SQLException {
        val howMany = 3;

        //given
        IntStream.range(0, howMany).forEach(_i -> {
            service.recordView(TEST_DOCUMENT, TEST_IP1);
            service.recordView(TEST_DOCUMENT, TEST_IP2);
            service.syncDB();
        });

        //when
        val amount = service.totalViews(TEST_DOCUMENT);

        //then
        assertThat(amount, equalTo(2 * howMany));
    }

    @Test
    void testCountDownloads() throws SQLException {
        val howMany = 3;

        //given
        IntStream.range(0, howMany).forEach(_i -> {
            service.recordDownload(TEST_DOCUMENT, TEST_IP1);
            service.recordDownload(TEST_DOCUMENT, TEST_IP2);
            service.syncDB();
        });

        //when
        val amount = service.totalDownloads(TEST_DOCUMENT);

        //then
        assertThat(amount, equalTo(2 * howMany));
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

    @Test
    void testGetMetricsReport() throws Exception {
        Statement statement = db.getConnection().createStatement();
        String sql = "insert into %s (start_timestamp, end_timestamp, document, amount, doc_title, record_type) values ('%s', '%s', '%s', %d, '%s', '%s')";
        statement.executeUpdate(String.format(sql, "views", "1721948400", "1722034799", "abcd1", 1, "test1", "a")); // 26July2024 timestsmp
        statement.executeUpdate(String.format(sql, "views", "1722034800", "1722121199", "abcd2", 2, "test2", "b")); // 27July2024 timestsmp
        statement.executeUpdate(String.format(sql, "views", "1722121200", "1722207599", "abcd3", 3, "test3", "c")); // 28July2024 timestsmp

        statement.executeUpdate(String.format(sql, "downloads", "1721948400", "1722034799", "abcd2", 1, "test2", "b")); // 26July2024 timestsmp
        statement.executeUpdate(String.format(sql, "downloads", "1722034800", "1722121199", "abcd3", 2, "test3", "c")); // 27July2024 timestsmp
        statement.executeUpdate(String.format(sql, "downloads", "1722121200", "1722207599", "abcd4", 3, "test4", "d")); // 28July2024 timestsmp

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<String> recordType = Arrays.asList("a", "c");
        String orderBy = "views";
        String ordering = "descending";
        String docId = "abcd1";
        Integer noOfRecords = 1;

        //when
        String result = service.getMetricsReport(df.parse("2024-07-29 00:00:00"), df.parse("2024-07-29 23:59:59"), orderBy, ordering, recordType, docId, noOfRecords).toString();
        //then
        assertThat(result, equalTo("[]"));  // no record in supply date

        Date startDate = df.parse("2024-07-26 00:00:00");
        Date endDate = df.parse("2024-07-27 23:59:59");
        result = service.getMetricsReport(startDate, null, null, null, null, null, null).toString();
        assertThat(result, equalTo("[" +
            "{downloads=0, doc_title=test1, document=abcd1, record_type=a, views=1}, " +
            "{downloads=1, doc_title=test2, document=abcd2, record_type=b, views=2}, " +
            "{downloads=2, doc_title=test3, document=abcd3, record_type=c, views=3}, " +
            "{downloads=3, doc_title=test4, document=abcd4, record_type=d, views=0}]"));

        result = service.getMetricsReport(startDate, endDate, null, null, null, null, null).toString();
        assertThat(result, equalTo("[" +
            "{downloads=0, doc_title=test1, document=abcd1, record_type=a, views=1}, " +
            "{downloads=1, doc_title=test2, document=abcd2, record_type=b, views=2}, " +
            "{downloads=2, doc_title=test3, document=abcd3, record_type=c, views=0}]"));

        result = service.getMetricsReport(startDate, endDate, orderBy, null, null, null, null).toString();
        assertThat(result, equalTo("[" +
            "{downloads=2, doc_title=test3, document=abcd3, record_type=c, views=0}, " +
            "{downloads=0, doc_title=test1, document=abcd1, record_type=a, views=1}, " +
            "{downloads=1, doc_title=test2, document=abcd2, record_type=b, views=2}]"));

        result = service.getMetricsReport(startDate, endDate, orderBy, ordering, null, null, null).toString();
        assertThat(result, equalTo("[" +
            "{downloads=1, doc_title=test2, document=abcd2, record_type=b, views=2}, " +
            "{downloads=0, doc_title=test1, document=abcd1, record_type=a, views=1}, " +
            "{downloads=2, doc_title=test3, document=abcd3, record_type=c, views=0}]"));

        orderBy = "downloads";
        result = service.getMetricsReport(startDate, endDate, orderBy, ordering, null, null, null).toString();
        assertThat(result, equalTo("[" +
            "{downloads=2, doc_title=test3, document=abcd3, record_type=c, views=0}, " +
            "{downloads=1, doc_title=test2, document=abcd2, record_type=b, views=2}, " +
            "{downloads=0, doc_title=test1, document=abcd1, record_type=a, views=1}]"));

        result = service.getMetricsReport(startDate, endDate, orderBy, ordering, recordType, null, null).toString();
        assertThat(result, equalTo("[" +
            "{downloads=2, doc_title=test3, document=abcd3, record_type=c, views=0}, " +
            "{downloads=0, doc_title=test1, document=abcd1, record_type=a, views=1}]"));

        result = service.getMetricsReport(startDate, endDate, orderBy, ordering, recordType, docId, null).toString();
        assertThat(result, equalTo("[" +
            "{downloads=0, doc_title=test1, document=abcd1, record_type=a, views=1}]"));

        docId = "abcd";
        result = service.getMetricsReport(startDate, endDate, orderBy, ordering, recordType, docId, noOfRecords).toString();
        assertThat(result, equalTo("[" +
            "{downloads=2, doc_title=test3, document=abcd3, record_type=c, views=0}]"));

    }
}
