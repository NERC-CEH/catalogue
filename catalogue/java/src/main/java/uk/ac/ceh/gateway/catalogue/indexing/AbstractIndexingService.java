package uk.ac.ceh.gateway.catalogue.indexing;

import java.io.IOException;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

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
 * @author cjohn
 * @param <D> type of documents which get indexed
 * @param <I> indexable representation of a given document
 */
@Data
@Slf4j
public abstract class AbstractIndexingService<D, I> implements DocumentIndexingService {
    private final BundledReaderService<D> reader;
    private final DocumentListingService listingService;
    private final DataRepository<?> repo;
    private final IndexGenerator<D, I> indexGenerator;
    
    public abstract void clearIndex() throws DocumentIndexingException;
    public abstract void index(I toIndex) throws Exception;
    public abstract void commit() throws DocumentIndexingException;
    
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
                index(indexGenerator.generateIndex(readDocument(document, revision)));
            }
            catch(Exception ex) {
                log.error("Failed to index: {}", document, ex);
                joinedException.addSuppressed(document, new DocumentIndexingException(
                    String.format("Failed to index %s : %s", document, ex.getMessage()), ex));
                log.error("Suppressed indexing errors", (Object[]) joinedException.getSuppressed());
            }
        });
        
        commit(); // Commit the changes

        //If an exception was supressed, then throw
        if(joinedException.getSuppressed().length != 0) {
            throw joinedException;
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
}
