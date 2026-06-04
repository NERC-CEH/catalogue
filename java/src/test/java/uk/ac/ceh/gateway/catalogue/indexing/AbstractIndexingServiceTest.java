package uk.ac.ceh.gateway.catalogue.indexing;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("AbstractIndexingService")
class AbstractIndexingServiceTest {

    private TestIndexingService service;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setup() {
        service = new TestIndexingService();

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
    @DisplayName("rebuilds when index is empty and succeeds without logging")
    void rebuildWhenEmpty() {
        service.indexEmpty = true;

        service.attemptIndexing();

        assertThat(service.rebuildCalled).isTrue();
        assertThat(logAppender.list).isEmpty();
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

        List<ILoggingEvent> warnings = logAppender.list.stream()
            .filter(e -> e.getLevel() == Level.WARN)
            .toList();
        assertThat(warnings).hasSize(2);
        assertThat(warnings.get(0).getFormattedMessage()).contains("doc1");
        assertThat(warnings.get(1).getFormattedMessage()).contains("doc2");
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

    private static class TestIndexingService extends AbstractIndexingService<MetadataDocument, Object> {
        boolean indexEmpty = true;
        boolean rebuildCalled = false;
        DocumentIndexingException rebuildException;

        @SuppressWarnings("unchecked")
        TestIndexingService() {
            super(
                (BundledReaderService<MetadataDocument>) org.mockito.Mockito.mock(BundledReaderService.class),
                org.mockito.Mockito.mock(DocumentListingService.class),
                (DataRepository<?>) org.mockito.Mockito.mock(DataRepository.class),
                (IndexGenerator<MetadataDocument, Object>) org.mockito.Mockito.mock(IndexGenerator.class)
            );
        }

        @Override public boolean isIndexEmpty() { return indexEmpty; }
        @Override public void unindexDocuments(List<String> ids) {}
        @Override protected void clearIndex() {}
        @Override protected void index(Object o) {}

        @Override
        @SneakyThrows
        public void rebuildIndex() {
            rebuildCalled = true;
            if (rebuildException != null) throw rebuildException;
        }
    }
}
