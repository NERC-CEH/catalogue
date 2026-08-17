package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;

import java.io.IOException;
import java.util.List;

/**
 * The following abstract class defines the common structure for a
 * DocumentIndexingService which hydrates documents using a BundledReaderService.
 * <p>
 * Ultimately, the class backs on to an implementation of IndexGenerator. This is
 * used to create a required index object from a document which has been read
 * using the BundledReaderService.
 * <p>
 * Implementations of this class need to define the way in which the generated
 * index (I) gets indexed.
 *
 * @param <D> type of documents which get indexed
 * @param <I> indexable representation of a given document
 */
@Slf4j
@ToString
public abstract class AbstractIndexingService<D, I> implements DocumentIndexingService {
    private final BundledReaderService<D> reader;
    private final DocumentListingService listingService;
    private final DataRepository<?> repo;
    private final IndexGenerator<D, I> indexGenerator;

    public AbstractIndexingService(
            BundledReaderService<D> reader,
            DocumentListingService listingService,
            DataRepository<?> repo,
            IndexGenerator<D, I> indexGenerator
    ) {
        this.reader = reader;
        this.listingService = listingService;
        this.repo = repo;
        this.indexGenerator = indexGenerator;
    }

    protected abstract void clearIndex() throws DocumentIndexingException;
    protected abstract void index(I toIndex) throws Exception;

    protected boolean canIndex(D doc) {
        return true;
    }

    @Override
    public void rebuildIndex() throws DocumentIndexingException {
        try {
            log.info("Rebuilding {} index", indexName());
            clearIndex();
            DataRevision<?> latestRevision = repo.getLatestRevision();
            if (latestRevision == null) {
                log.warn("Cannot rebuild {} index: no revision available from the data repository", indexName());
                return;
            }
            String revision = latestRevision.getRevisionID();
            val documents = listingService.filterFilenames(repo.getFiles(revision));
            log.info("Rebuilding {} index with {} documents at revision {}", indexName(), documents.size(), revision);
            indexDocuments(documents, revision);
            log.info("Rebuilt {} index with {} documents at revision {}", indexName(), documents.size(), revision);
        }
        catch(IOException ex) {
            throw new DocumentIndexingException(ex);
        }
    }

    @Override
    public void indexDocuments(List<String> documents, String revision) throws DocumentIndexingException {
        DocumentIndexingException joinedException = new DocumentIndexingException("Failed to index one or more documents");
        documents.forEach((document) -> {
            try {
                log.debug("Indexing: {}, revision: {}", document, revision);
                val doc = readDocument(document, revision);
                if (canIndex(doc)) {
                    val toIndex = indexGenerator.generateIndex(doc);
                    index(toIndex);
                } else {
                    log.debug("Not indexing {}", document);
                }
            }
            catch(Exception ex) {
                joinedException.addSuppressed(
                    document,
                    new DocumentIndexingException(
                        String.format("Failed to index %s : %s", document, ex.getMessage()), ex
                    )
                );
            }
        });

        if (hasSuppressedExceptions(joinedException)) {
            throw joinedException;
        }
    }

    private boolean hasSuppressedExceptions(DocumentIndexingException joinedException) {
        return joinedException.getSuppressed().length != 0;
    }

    @Override
    public boolean attemptIndexing() {
        try {
            if(this.isIndexEmpty()) {
                log.info("{} index is empty, rebuilding", indexName());
                this.rebuildIndex();
            } else {
                log.debug("{} index already holds documents, not rebuilding", indexName());
            }
            return true;
        } catch (Exception ex) {
            if (ex.getSuppressed().length == 0) {
                // Nothing was indexed at all - typically the index is unreachable. Log the failure
                // itself, otherwise an empty index leaves no trace whatsoever in the log.
                // toString() rather than getMessage(): a bare ConnectException has no message at all
                log.warn("Could not index into {}: {}", indexName(), ex.toString(), ex);
                return false;
            }
            log.warn(
                "{} index rebuilt, but {} document(s) failed to index",
                indexName(), ex.getSuppressed().length
            );
            for (Throwable suppressed : ex.getSuppressed()) {
                log.warn("Indexing error: {}", suppressed.getMessage());
            }
            // The index was reached and rebuilt; retrying would clear and rebuild it again for the
            // sake of documents that will fail every time.
            return true;
        }
    }

    private String indexName() {
        return getClass().getSimpleName();
    }

    /**
     * An overridable method which uses the message bundle reader to load a
     * particular document.
     * <p>
     * Subclasses are free to adjust this method to add postprocessing
     * capabilities to the reading logic
     * @param document id of the document to read
     * @param revision the revision which to read at
     * @return a document which has been read
     */
    @SneakyThrows
    protected D readDocument(String document, String revision) {
        log.debug("Reading {} at revision {}", document, revision);
        return reader.readBundle(document, revision);
    }

    @SneakyThrows
    protected D readDocument(String document) {
        return reader.readBundle(document);
    }
}
