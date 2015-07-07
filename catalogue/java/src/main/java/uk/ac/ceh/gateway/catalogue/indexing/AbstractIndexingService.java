package uk.ac.ceh.gateway.catalogue.indexing;

import java.io.IOException;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

/**
 *
 * @author cjohn
 * @param <D>
 * @param <I>
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
            String revision = repo.getLatestRevision().getRevisionID();
            indexDocuments(listingService.filterFilenames(repo.getFiles(revision)), revision);
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
                index(indexGenerator.generateIndex(
                        reader.readBundle(document, revision)));
            }
            catch(Exception ex) {
                log.error("Failed to index: {}", document, ex);
                joinedException.addSuppressed(new DocumentIndexingException(
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
}
