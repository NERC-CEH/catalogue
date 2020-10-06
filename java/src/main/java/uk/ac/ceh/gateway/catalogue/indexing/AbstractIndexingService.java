package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.upload.UploadDocument;

import java.io.IOException;
import java.util.List;

/**
 * The following abstract class defines the common structure for a 
 * DocumentIndexingService which hydrates documents using a BundledReaderService.
 * 
 * Ultimately the class backs on to an implementation of IndexGenerator. This is 
 * used to create a required index object from a document which has been read 
 * using the BundledReaderService.
 * 
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
        log.info("Creating {}", this);
    }

    protected abstract void clearIndex() throws DocumentIndexingException;
    protected abstract void index(I toIndex) throws Exception;
    
    @Override
    public void rebuildIndex() throws DocumentIndexingException {
        try {
            clearIndex();
            DataRevision<?> latestRevision = repo.getLatestRevision();
            if (latestRevision != null) {
                String revision = latestRevision.getRevisionID();
                indexDocuments(listingService.filterFilenames(repo.getFiles(revision)), revision);
            }
        }
        catch(IOException ex) {
            throw new DocumentIndexingException(ex);
        }
    }
    
    @Override
    public void indexDocuments(List<String> documents, String revision) throws DocumentIndexingException {
        DocumentIndexingException joinedException = new DocumentIndexingException("Failed to index one or more documents");
        documents.stream().forEach((document) -> {
            try {
                log.debug("Indexing: {}, revision: {}", document, revision);
                val doc = readDocument(document, revision);
                if (!(doc instanceof UploadDocument)) index(indexGenerator.generateIndex(doc));
            }
            catch(Exception ex) {
                joinedException.addSuppressed(document, new DocumentIndexingException(
                    String.format("Failed to index %s : %s", document, ex.getMessage()), ex));
            }
        });

        //If an exception was supressed, then throw
        if(joinedException.getSuppressed().length != 0) {
            throw joinedException;
        }
    }

    public void initialIndex() {
        try {
            if(this.isIndexEmpty()) {
                this.rebuildIndex();
            }
        } catch (Exception ex) {
            log.error("There were records that did not index successfully at container creation. This does not stop the container starting");
            log.error("Suppressed indexing errors", (Object[]) ex.getSuppressed());
        }
    }
    
    /**
     * An overridable method which uses the message bundle reader to load a 
     * particular document.
     * 
     * Sub classes are free to adjust this method to add postprocessing 
     * capabilities to the reading logic
     * @param document id of the document to read
     * @param revision the revision which to read at
     * @return a document which has been read
     * @throws Exception if something went wrong
     */
    protected D readDocument(String document, String revision) throws Exception {
        return reader.readBundle(document, revision);
    }
    
    protected D readDocument(String document) throws Exception {
        return reader.readBundle(document);
    }
}
