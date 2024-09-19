package uk.ac.ceh.gateway.catalogue.metrics;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JDBCMetricsServiceTest {
    private EmbeddedDatabase db;
    private JDBCMetricsService service;

    private static final String TEST_DOCUMENT = "123e4567-e89b-12d3-a456-426614174000";
    private static final String TEST_IP1 = "192.0.2.1";
    private static final String TEST_IP2 = "192.0.2.2";
    @Mock private DocumentRepository documentRepository;
    @Mock private JdbcTemplate jdbcTemplate;
    private final MetadataDocument doc = new GeminiDocument();

    @BeforeEach
    void setup() {
        db = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .generateUniqueName(true)
            .build();
        service = new JDBCMetricsService(db, documentRepository);
        doc.setTitle("default Test Title");
        doc.setType("default dataset");
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

    @SneakyThrows
    @Test
    void testRecordView() {
        //given
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        //when
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.recordView(TEST_DOCUMENT, TEST_IP2);
        service.syncDB();

        //then
        val rows = getDocumentsAndAmounts("views");
        assertThat(rows, contains(contains(TEST_DOCUMENT, 2)));
    }

    @SneakyThrows
    @Test
    void testRecordDownload() {
        //given
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        //when
        service.recordDownload(TEST_DOCUMENT, TEST_IP1);
        service.recordDownload(TEST_DOCUMENT, TEST_IP1);
        service.recordDownload(TEST_DOCUMENT, TEST_IP2);
        service.syncDB();

        //then
        val rows = getDocumentsAndAmounts("downloads");
        assertThat(rows, contains(contains(TEST_DOCUMENT, 2)));
    }

    @SneakyThrows
    @Test
    void testCountViews() {
        val howMany = 3;
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
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

    @SneakyThrows
    @Test
    void testCountDownloads() {
        val howMany = 3;
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
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

    @SneakyThrows
    @Test
    void testUpdateView() {
        //given
        given(documentRepository.read(anyString())).willReturn(doc);

        //when
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();
        val preRows = getTitleAndType("views");
        assertThat(preRows, contains(contains(doc.getTitle(), doc.getType())));

        doc.setTitle("updated Test Title");
        doc.setType("updated dataset");
        service.updateDB();

        //then
        val postRows = getTitleAndType("views");
        assertThat(postRows, contains(contains(doc.getTitle(), doc.getType())));
    }

    @SneakyThrows
    @Test
    void testUpdateDownload() {
        //given
        given(documentRepository.read(anyString())).willReturn(doc);

        //when
        service.recordDownload(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();
        val preRows = getTitleAndType("downloads");
        assertThat(preRows, contains(contains(doc.getTitle(), doc.getType())));

        doc.setTitle("updated Test Title");
        doc.setType("updated dataset");
        service.updateDB();

        //then
        val postRows = getTitleAndType("downloads");
        assertThat(postRows, contains(contains(doc.getTitle(), doc.getType())));
    }

    @SneakyThrows
    @Test
    void testUpdateViewThrows() {
        //given
        given(documentRepository.read(anyString())).willReturn(doc).willThrow(new RuntimeException("Oops"));

        //when
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();
        service.updateDB();

        //then
        verify(documentRepository, times(2)).read(anyString());
        verify(jdbcTemplate, times(0)).update(anyString(), anyString(), anyString(), anyString());

    }

    @SneakyThrows
    @Test
    void testUpdateDownloadThrows() {
        //given
        given(documentRepository.read(anyString())).willReturn(doc);

        //when
        service.recordDownload(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();
        service.updateDB();

        //then
        verify(documentRepository, times(2)).read(anyString());
        verify(jdbcTemplate, times(0)).update(anyString(), anyString(), anyString(), anyString());

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
        statement.executeUpdate(String.format(sql, "views", "1721952000", "1722038399", "abcd1", 1, "test1", "a")); // 26July2024 timestsmp
        statement.executeUpdate(String.format(sql, "views", "1722038400", "1722124799", "abcd2", 2, "test2", "b")); // 27July2024 timestsmp
        statement.executeUpdate(String.format(sql, "views", "1722124800", "1722211199", "abcd3", 3, "test3", "c")); // 28July2024 timestsmp

        statement.executeUpdate(String.format(sql, "downloads", "1721952000", "1722038399", "abcd2", 1, "test2", "b")); // 26July2024 timestsmp
        statement.executeUpdate(String.format(sql, "downloads", "1722038400", "1722124799", "abcd3", 2, "test3", "c")); // 27July2024 timestsmp
        statement.executeUpdate(String.format(sql, "downloads", "1722124800", "1722211199", "abcd4", 3, "test4", "d")); // 28July2024 timestsmp

        List<String> recordType = Arrays.asList("a", "c");
        String orderBy = "views";
        String ordering = "descending";
        String docId = "abcd1";
        Integer noOfRecords = 1;

        //when
        String result = service.getMetricsReport(Instant.parse("2024-07-29T00:00:00Z"), Instant.parse("2024-07-29T23:59:59.00Z"), orderBy, ordering, recordType, docId, noOfRecords).toString();
        //then
        assertThat(result, equalTo("[]"));  // no record in supply date

        Instant startDate = Instant.parse("2024-07-26T00:00:00.00Z");
        Instant endDate = Instant.parse("2024-07-27T23:59:59.00Z");
        result = service.getMetricsReport(startDate, null, null, null, null, null, null).toString();
        assertThat(result, equalTo("[" +
            "{document=abcd1, docTitle=test1, recordType=a, views=1, downloads=0}, " +
            "{document=abcd2, docTitle=test2, recordType=b, views=2, downloads=1}, " +
            "{document=abcd3, docTitle=test3, recordType=c, views=3, downloads=2}, " +
            "{document=abcd4, docTitle=test4, recordType=d, views=0, downloads=3}]"));

        result = service.getMetricsReport(startDate, endDate, null, null, null, null, null).toString();
        assertThat(result, equalTo("[" +
            "{document=abcd1, docTitle=test1, recordType=a, views=1, downloads=0}, " +
            "{document=abcd2, docTitle=test2, recordType=b, views=2, downloads=1}, " +
            "{document=abcd3, docTitle=test3, recordType=c, views=0, downloads=2}]"));

        result = service.getMetricsReport(startDate, endDate, orderBy, null, null, null, null).toString();
        assertThat(result, equalTo("[" +
            "{document=abcd3, docTitle=test3, recordType=c, views=0, downloads=2}, " +
            "{document=abcd1, docTitle=test1, recordType=a, views=1, downloads=0}, " +
            "{document=abcd2, docTitle=test2, recordType=b, views=2, downloads=1}]"));

        result = service.getMetricsReport(startDate, endDate, orderBy, ordering, null, null, null).toString();
        assertThat(result, equalTo("[" +
            "{document=abcd2, docTitle=test2, recordType=b, views=2, downloads=1}, " +
            "{document=abcd1, docTitle=test1, recordType=a, views=1, downloads=0}, " +
            "{document=abcd3, docTitle=test3, recordType=c, views=0, downloads=2}]"));

        orderBy = "downloads";
        result = service.getMetricsReport(startDate, endDate, orderBy, ordering, null, null, null).toString();
        assertThat(result, equalTo("[" +
            "{document=abcd3, docTitle=test3, recordType=c, views=0, downloads=2}, " +
            "{document=abcd2, docTitle=test2, recordType=b, views=2, downloads=1}, " +
            "{document=abcd1, docTitle=test1, recordType=a, views=1, downloads=0}]"));

        result = service.getMetricsReport(startDate, endDate, orderBy, ordering, recordType, null, null).toString();
        assertThat(result, equalTo("[" +
            "{document=abcd3, docTitle=test3, recordType=c, views=0, downloads=2}, " +
            "{document=abcd1, docTitle=test1, recordType=a, views=1, downloads=0}]"));

        result = service.getMetricsReport(startDate, endDate, orderBy, ordering, recordType, docId, null).toString();
        assertThat(result, equalTo("[" +
            "{document=abcd1, docTitle=test1, recordType=a, views=1, downloads=0}]"));

        docId = "abcd";
        result = service.getMetricsReport(startDate, endDate, orderBy, ordering, recordType, docId, noOfRecords).toString();
        assertThat(result, equalTo("[" +
            "{document=abcd3, docTitle=test3, recordType=c, views=0, downloads=2}]"));

    }

    List<List<Object>> getTitleAndType(String table) throws SQLException {
        val stmt = db.getConnection().createStatement();
        val rs = stmt.executeQuery("SELECT doc_title, record_type FROM " + table);
        val rows = new ArrayList<List<Object>>();
        while (rs.next()) {
            rows.add(List.of(rs.getString(1), rs.getString(2)));
        }
        return rows;
    }
}
