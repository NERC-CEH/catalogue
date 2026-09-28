package uk.ac.ceh.gateway.catalogue.metrics;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
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
    private SeverableDataSource dataSource;
    private JDBCMetricsService service;

    private static final String TEST_DOCUMENT = "123e4567-e89b-12d3-a456-426614174000";
    private static final String TEST_IP1 = "192.0.2.1";
    private static final String TEST_IP2 = "192.0.2.2";
    @Mock private DocumentRepository documentRepository;
    private final MetadataDocument doc = new GeminiDocument();

    /**
     * The service is given a datasource that can be severed after construction rather than the embedded
     * database directly, so that the unavailable-database case can be reproduced honestly — see
     * {@link #totalsAreAbsentRatherThanThrowingWhenTheDatabaseIsUnavailable}. Every other test is
     * unaffected: until it is severed the wrapper is a pass-through.
     */
    @BeforeEach
    void setup() {
        db = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .generateUniqueName(true)
            .build();
        dataSource = new SeverableDataSource(db);
        service = new JDBCMetricsService(dataSource, documentRepository);
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
        val tables = new ArrayList<String>();
        try (Connection connection = db.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", null)) {
                while (rs.next()) {
                    tables.add(rs.getString(3));
                }
            }
        }

        assertThat(tables, hasItems(equalToIgnoringCase("views"), equalToIgnoringCase("downloads")));
    }

    /**
     * Both count queries filter on {@code document}. Without an index that is a full table scan, which
     * in production means scanning a 1.1 GB SQLite file across a CIFS mount — measured at roughly 7
     * seconds per scan, twice per record page render. Asserting the index exists rather than trusting
     * the DDL, because it was absent from the live database for the table's entire lifetime.
     *
     * <p>{@code amount} is asserted too: it is what makes the index covering for
     * {@code sum(amount) ... WHERE document = ?}, so dropping it would quietly reintroduce a table
     * lookup per matching row while still leaving this test's {@code document} assertion satisfied.</p>
     */
    @SneakyThrows
    @Test
    void countTablesCarryACoveringIndexForTheTotalsQuery() {
        //given/when the service has initialised its schema

        //then each count table's index covers both the filter column and the summed column
        assertThat(indexedColumnsOf("views"),
            hasItems(equalToIgnoringCase("document"), equalToIgnoringCase("amount")));
        assertThat(indexedColumnsOf("downloads"),
            hasItems(equalToIgnoringCase("document"), equalToIgnoringCase("amount")));
    }

    private List<String> indexedColumnsOf(String table) throws SQLException {
        val columns = new ArrayList<String>();
        try (Connection connection = db.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getIndexInfo(null, null, table.toUpperCase(Locale.ROOT), false, false)) {
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME"));
                }
            }
        }
        return columns;
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

    /**
     * A view counter is decorative, but it is read while rendering a record page, so a failure here
     * used to propagate out through FreeMarker and 500 the whole page. In production that happened
     * whenever a read met the hourly sync's lock on the SQLite file (SQLITE_BUSY); now the database is
     * remote it will happen on an unreachable host, an exhausted pool or a failover instead. The count
     * must come back absent either way, leaving the page to render without it.
     *
     * <p>The connection is severed rather than the embedded database shut down.
     * {@code EmbeddedDatabaseBuilder} sets {@code DB_CLOSE_DELAY=-1}, so after a shutdown the next
     * connection succeeds against a fresh, empty database: that exercises a missing-table
     * {@code BadSqlGrammarException}, not the unreachable-database path this test is named for. Both are
     * {@code DataAccessException}s, so the test passed regardless of whether the catch actually covered
     * the case that matters.</p>
     */
    @Test
    void totalsAreAbsentRatherThanThrowingWhenTheDatabaseIsUnavailable() {
        //given the database cannot be reached
        dataSource.sever();

        //when/then no exception escapes, and the counts report themselves as unavailable
        assertThat(service.totalViews(TEST_DOCUMENT), is(nullValue()));
        assertThat(service.totalDownloads(TEST_DOCUMENT), is(nullValue()));
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

    /**
     * A repository failure for one document must not abort the nightly title refresh or escape the
     * scheduler; the row simply keeps the title it already had until the next run.
     *
     * <p>Asserted against the table rather than against a mocked {@code JdbcTemplate}. The service builds
     * its own template from the {@code DataSource}, so a mock of that type is never reached by anything
     * the service does and {@code verify(..., times(0))} on it holds no matter how the code behaves.</p>
     */
    @SneakyThrows
    @Test
    void testUpdateViewThrows() {
        //given the second repository read fails
        given(documentRepository.read(anyString())).willReturn(doc).willThrow(new RuntimeException("Oops"));
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();

        //when
        service.updateDB();

        //then the failure is swallowed and the existing row is left untouched
        verify(documentRepository, times(2)).read(anyString());
        assertThat(getTitleAndType("views"), contains(contains("default Test Title", "default dataset")));
    }

    @SneakyThrows
    @Test
    void testUpdateDownloadThrows() {
        //given the second repository read fails
        given(documentRepository.read(anyString())).willReturn(doc).willThrow(new RuntimeException("Oops"));
        service.recordDownload(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();

        //when
        service.updateDB();

        //then the failure is swallowed and the existing row is left untouched
        verify(documentRepository, times(2)).read(anyString());
        assertThat(getTitleAndType("downloads"), contains(contains("default Test Title", "default dataset")));
    }

    @Test
    void hasMetricsForIsFalseWhenNothingIsRecorded() {
        //given/when no view or download has ever been recorded for this document

        //then
        assertThat(service.hasMetricsFor(TEST_DOCUMENT), is(false));
    }

    @SneakyThrows
    @Test
    void hasMetricsForIsTrueWhenOnlyAViewIsRecorded() {
        //given
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();

        //when/then
        assertThat(service.hasMetricsFor(TEST_DOCUMENT), is(true));
    }

    @SneakyThrows
    @Test
    void hasMetricsForIsTrueWhenOnlyADownloadIsRecorded() {
        //given
        given(documentRepository.read(TEST_DOCUMENT)).willReturn(doc);
        service.recordDownload(TEST_DOCUMENT, TEST_IP1);
        service.syncDB();

        //when/then
        assertThat(service.hasMetricsFor(TEST_DOCUMENT), is(true));
    }

    /**
     * Deletion is by {@code document} only, so a row belonging to a different document must survive —
     * this is what stops an admin-delete of one orphaned record from wiping every other document's
     * counts out of the same table.
     */
    @SneakyThrows
    @Test
    void deleteMetricsForRemovesOnlyTheGivenDocumentsRowsFromBothTables() {
        //given
        String otherDocument = "123e4567-e89b-12d3-a456-426614174999";
        given(documentRepository.read(anyString())).willReturn(doc);
        service.recordView(TEST_DOCUMENT, TEST_IP1);
        service.recordDownload(TEST_DOCUMENT, TEST_IP1);
        service.recordView(otherDocument, TEST_IP1);
        service.syncDB();

        //when
        boolean result = service.deleteMetricsFor(TEST_DOCUMENT);

        //then
        assertThat(result, is(true));
        assertThat(getDocumentsAndAmounts("views"), contains(contains(otherDocument, 1)));
        assertThat(getDocumentsAndAmounts("downloads"), is(empty()));
        assertThat(service.hasMetricsFor(TEST_DOCUMENT), is(false));
    }

    @Test
    void deleteMetricsForReturnsFalseWhenThereWasNothingToDelete() {
        //given/when no rows exist for this document

        //then
        assertThat(service.deleteMetricsFor(TEST_DOCUMENT), is(false));
    }

    List<List<Object>> getDocumentsAndAmounts(String table) throws SQLException {
        val rows = new ArrayList<List<Object>>();
        try (Connection connection = db.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT document, amount FROM " + table)) {
            while (rs.next()) {
                rows.add(List.of(rs.getString(1), rs.getInt(2)));
            }
        }
        return rows;
    }

    @Test
    void testGetMetricsReport() throws Exception {
        String sql = "insert into %s (start_timestamp, end_timestamp, document, amount, doc_title, record_type) values ('%s', '%s', '%s', %d, '%s', '%s')";
        try (Connection connection = db.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(String.format(sql, "views", "1721952000", "1722038399", "abcd1", 1, "test1", "a")); // 26July2024 timestsmp
            statement.executeUpdate(String.format(sql, "views", "1722038400", "1722124799", "abcd2", 2, "test2", "b")); // 27July2024 timestsmp
            statement.executeUpdate(String.format(sql, "views", "1722124800", "1722211199", "abcd3", 3, "test3", "c")); // 28July2024 timestsmp

            statement.executeUpdate(String.format(sql, "downloads", "1721952000", "1722038399", "abcd2", 1, "test2", "b")); // 26July2024 timestsmp
            statement.executeUpdate(String.format(sql, "downloads", "1722038400", "1722124799", "abcd3", 2, "test3", "c")); // 27July2024 timestsmp
            statement.executeUpdate(String.format(sql, "downloads", "1722124800", "1722211199", "abcd4", 3, "test4", "d")); // 28July2024 timestsmp
        }

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

    /**
     * NOT YET A TEST OF POSTGRESQL. Disabled deliberately rather than deleted, to keep the gap visible.
     *
     * <p>The suite runs entirely on H2, so the report query has never met the engine it is being migrated
     * to (#236). Two things are known to differ and neither is covered:</p>
     *
     * <ul>
     *   <li>{@code getMetricsReport} binds every parameter with {@code setString}, including
     *       {@code start_timestamp} and {@code end_timestamp}, which are {@code integer} columns. H2
     *       coerces; PostgreSQL rejects {@code integer >= varchar} with "operator does not exist"
     *       (SQLSTATE 42883). If that is confirmed, the fix belongs in the service — bind the timestamps
     *       as {@code long} — not in the test.</li>
     *   <li>{@code SimpleJdbcInsert} reads column metadata from the driver, and identifier casing and
     *       metadata behaviour differ between the two engines.</li>
     * </ul>
     *
     * <p>H2's {@code MODE=PostgreSQL} would not settle either question — it does not reproduce the type
     * resolution rules these depend on, and {@code EmbeddedDatabaseBuilder} cannot set the mode in any
     * case. This needs Testcontainers against the same {@code postgres:14.1-alpine} image as the
     * {@code metrics-db} compose service; enable this test as part of that work.</p>
     */
    @Disabled("Needs Testcontainers against postgres:14.1-alpine - H2 cannot answer this. See #236.")
    @Test
    void reportQueryRunsAgainstRealPostgres() {
        throw new UnsupportedOperationException("Pending Testcontainers support for the metrics database");
    }

    List<List<Object>> getTitleAndType(String table) throws SQLException {
        val rows = new ArrayList<List<Object>>();
        try (Connection connection = db.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT doc_title, record_type FROM " + table)) {
            while (rs.next()) {
                rows.add(List.of(rs.getString(1), rs.getString(2)));
            }
        }
        return rows;
    }

    /**
     * Pass-through to the embedded database until {@link #sever()} is called, after which every
     * connection attempt fails as it would against an unreachable host or an exhausted pool. This is what
     * lets the read-path guard be tested on the failure it actually exists for, without the connection
     * simply being re-established against an empty database.
     */
    private static class SeverableDataSource extends AbstractDataSource {
        private final DataSource delegate;
        private boolean severed;

        SeverableDataSource(DataSource delegate) {
            this.delegate = delegate;
        }

        void sever() {
            this.severed = true;
        }

        @Override
        public Connection getConnection() throws SQLException {
            if (severed) {
                throw new SQLException("Connection to metrics database refused");
            }
            return delegate.getConnection();
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            if (severed) {
                throw new SQLException("Connection to metrics database refused");
            }
            return delegate.getConnection(username, password);
        }
    }
}
