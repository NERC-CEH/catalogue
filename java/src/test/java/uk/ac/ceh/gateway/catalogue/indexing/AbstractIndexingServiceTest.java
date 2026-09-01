package uk.ac.ceh.gateway.catalogue.indexing;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("AbstractIndexingService")
class AbstractIndexingServiceTest {

    private BundledReaderService<MetadataDocument> reader;
    private DocumentListingService listingService;
    private DataRepository<CatalogueUser> repo;
    private IndexGenerator<MetadataDocument, Object> indexGenerator;

    private TestIndexingService service;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        reader = mock(BundledReaderService.class);
        listingService = mock(DocumentListingService.class);
        repo = mock(DataRepository.class);
        indexGenerator = mock(IndexGenerator.class);
        service = new TestIndexingService(reader, listingService, repo, indexGenerator);

        logAppender = new ListAppender<>();
        logAppender.start();
        ((Logger) LoggerFactory.getLogger(AbstractIndexingService.class)).addAppender(logAppender);
    }

    @AfterEach
    void teardown() {
        ((Logger) LoggerFactory.getLogger(AbstractIndexingService.class)).detachAppender(logAppender);
    }

    @Test
    @DisplayName("does not rebuild when index is not empty")
    void noRebuildWhenNotEmpty() {
        service.indexEmpty = false;

        service.attemptIndexing();

        assertThat(service.rebuildCalled).isFalse();
    }

    @Test
    @DisplayName("reports success when the index is already populated")
    void reportsSuccessWhenIndexPopulated() {
        service.indexEmpty = false;

        assertThat(service.attemptIndexing()).isTrue();
    }

    @Test
    @DisplayName("rebuilds when index is empty")
    void rebuildWhenEmpty() {
        service.indexEmpty = true;

        assertThat(service.attemptIndexing()).isTrue();

        assertThat(service.rebuildCalled).isTrue();
    }

    @Test
    @DisplayName("swallows suppressed indexing exceptions without rethrowing")
    void swallowsSuppressedExceptions() {
        DocumentIndexingException ex = new DocumentIndexingException("Failed to index one or more documents");
        ex.addSuppressed("doc1", new DocumentIndexingException("doc1: methodrecord does not have a corresponding class"));
        service.rebuildException = ex;

        assertDoesNotThrow(() -> service.attemptIndexing());
    }

    @Test
    @DisplayName("logs one warn per suppressed exception with the exception message")
    void logsWarnPerSuppressedException() {
        DocumentIndexingException ex = new DocumentIndexingException("Failed to index one or more documents");
        ex.addSuppressed("doc1", new DocumentIndexingException("Failed to index doc1 : type unknown"));
        ex.addSuppressed("doc2", new DocumentIndexingException("Failed to index doc2 : type unknown"));
        service.rebuildException = ex;

        service.attemptIndexing();

        List<ILoggingEvent> warnings = warnings();
        assertThat(warnings).anyMatch(e -> e.getFormattedMessage().contains("doc1"));
        assertThat(warnings).anyMatch(e -> e.getFormattedMessage().contains("doc2"));
    }

    @Test
    @DisplayName("does not log at ERROR level for suppressed exceptions")
    void noErrorLevelForSuppressedExceptions() {
        DocumentIndexingException ex = new DocumentIndexingException("Failed to index one or more documents");
        ex.addSuppressed("doc1", new DocumentIndexingException("Failed to index doc1 : type unknown"));
        service.rebuildException = ex;

        service.attemptIndexing();

        assertThat(logAppender.list).noneMatch(e -> e.getLevel() == Level.ERROR);
    }

    @Test
    @DisplayName("reports success when a rebuild only had individual document failures")
    void reportsSuccessWhenOnlyDocumentsFailed() {
        DocumentIndexingException ex = new DocumentIndexingException("Failed to index one or more documents");
        ex.addSuppressed("doc1", new DocumentIndexingException("Failed to index doc1 : type unknown"));
        service.rebuildException = ex;

        assertThat(service.attemptIndexing()).isTrue();
    }

    @Test
    @DisplayName("logs the exception itself when the index cannot be reached")
    void logsExceptionWhenIndexUnreachable() {
        service.isIndexEmptyException = new DocumentIndexingException(new RuntimeException("Connection refused"));

        service.attemptIndexing();

        assertThat(warnings())
            .anyMatch(e -> e.getFormattedMessage().contains("Connection refused")
                || (e.getThrowableProxy() != null && e.getThrowableProxy().getMessage().contains("Connection refused")));
        assertThat(warnings()).anyMatch(e -> e.getThrowableProxy() != null);
    }

    @Test
    @DisplayName("names the exception when the failure carries no message of its own")
    void logsExceptionTypeWhenMessageIsNull() {
        service.isIndexEmptyException = new ConnectException();

        service.attemptIndexing();

        assertThat(warnings()).anyMatch(e -> e.getFormattedMessage().contains("ConnectException"));
    }

    @Test
    @DisplayName("reports failure when the index cannot be reached")
    void reportsFailureWhenIndexUnreachable() {
        service.isIndexEmptyException = new DocumentIndexingException(new RuntimeException("Connection refused"));

        assertThat(service.attemptIndexing()).isFalse();
        assertThat(service.rebuildCalled).isFalse();
    }

    @Test
    @DisplayName("reports failure when a rebuild fails outright")
    void reportsFailureWhenRebuildFailsOutright() {
        service.rebuildException = new DocumentIndexingException(new RuntimeException("Connection refused"));

        assertThat(service.attemptIndexing()).isFalse();
    }

    @Test
    @DisplayName("logs rebuild start and completion at INFO with a document count")
    @SneakyThrows
    void logsRebuildStartAndCompletionWithCount() {
        givenRepositoryContains("doc1", "doc2", "doc3");

        service.rebuildIndex();

        List<String> infos = logAppender.list.stream()
            .filter(e -> e.getLevel() == Level.INFO)
            .map(ILoggingEvent::getFormattedMessage)
            .toList();
        assertThat(infos).anyMatch(m -> m.contains("Rebuilding") && m.contains("TestIndexingService"));
        assertThat(infos).anyMatch(m -> m.contains("Rebuilt") && m.contains("3"));
    }

    @Test
    @DisplayName("warns when there is no revision to index")
    @SneakyThrows
    void warnsWhenNoRevisionAvailable() {
        given(repo.getLatestRevision()).willReturn(null);

        service.rebuildIndex();

        assertThat(warnings()).anyMatch(e -> e.getFormattedMessage().contains("no revision"));
    }

    @Test
    @DisplayName("does not clear the index when the data repository cannot be read")
    @SneakyThrows
    void doesNotClearIndexWhenRepositoryUnreadable() {
        // A datastore whose pack index is missing throws on the very first read. Clearing before
        // that read would empty a perfectly good index with nothing available to repopulate it.
        given(repo.getLatestRevision()).willThrow(
            new DataRepositoryException("Missing unknown 235ad70dcb07283c03b913879e249a8b6b94b49d")
        );

        assertThrows(DocumentIndexingException.class, () -> service.rebuildIndex());

        assertThat(service.events).doesNotContain("clear");
    }

    @Test
    @DisplayName("does not clear the index when there is no revision to index")
    @SneakyThrows
    void doesNotClearIndexWhenNoRevisionAvailable() {
        given(repo.getLatestRevision()).willReturn(null);

        service.rebuildIndex();

        assertThat(service.events).doesNotContain("clear");
    }

    @Test
    @DisplayName("clears the index before indexing once the repository has been read")
    @SneakyThrows
    void clearsIndexBeforeIndexingOnRebuild() {
        givenRepositoryContains("doc1", "doc2");

        service.rebuildIndex();

        assertThat(service.events).containsExactly("clear", "index", "index");
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private void givenRepositoryContains(String... documents) {
        val revision = (DataRevision<CatalogueUser>) mock(DataRevision.class);
        given(revision.getRevisionID()).willReturn("latest");
        given(repo.getLatestRevision()).willReturn(revision);
        given(listingService.filterFilenames(any())).willReturn(List.of(documents));
        given(reader.readBundle(anyString(), anyString())).willReturn(mock(MetadataDocument.class));
    }

    private List<ILoggingEvent> warnings() {
        return logAppender.list.stream()
            .filter(e -> e.getLevel() == Level.WARN)
            .toList();
    }

    private static class TestIndexingService extends AbstractIndexingService<MetadataDocument, Object> {
        boolean indexEmpty = true;
        boolean rebuildCalled = false;
        final List<String> events = new ArrayList<>();
        DocumentIndexingException rebuildException;
        Exception isIndexEmptyException;

        TestIndexingService(
            BundledReaderService<MetadataDocument> reader,
            DocumentListingService listingService,
            DataRepository<?> repo,
            IndexGenerator<MetadataDocument, Object> indexGenerator
        ) {
            super(reader, listingService, repo, indexGenerator);
        }

        @Override
        @SneakyThrows
        public boolean isIndexEmpty() throws DocumentIndexingException {
            if (isIndexEmptyException != null) throw isIndexEmptyException;
            return indexEmpty;
        }

        @Override public void unindexDocuments(List<String> ids) {}
        @Override protected void clearIndex() { events.add("clear"); }
        @Override protected void index(Object o) { events.add("index"); }

        @Override
        @SneakyThrows
        public void rebuildIndex() {
            rebuildCalled = true;
            if (rebuildException != null) throw rebuildException;
            super.rebuildIndex();
        }
    }
}
