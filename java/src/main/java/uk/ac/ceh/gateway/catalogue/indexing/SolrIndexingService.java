package uk.ac.ceh.gateway.catalogue.indexing;

import java.io.IOException;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

/**
 *
 * @author cjohn
 * @param <D>
 */
@Data
@Slf4j
public class SolrIndexingService<D> implements DocumentIndexingService {
    private final BundledReaderService<D> reader;
    private final DocumentListingService listingService;
    private final DataRepository<?> repo;
    private final SolrIndexGenerator<D> indexGenerator;
    private final SolrServer solrServer;
    
    @Override
    public void rebuildIndex() throws DocumentIndexingException {
        try {
            solrServer.deleteByQuery("*:*");
            String revision = repo.getLatestRevision().getRevisionID();
            indexDocuments(listingService.filterFilenames(repo.getFiles(revision)), revision);
        }
        catch(IOException | SolrServerException  ex) {
            throw new DocumentIndexingException(ex);
        }
    }
    
    @Override
    public boolean isIndexEmpty() throws DocumentIndexingException {
        try {
            return solrServer.query(new SolrQuery("*:*")).getResults().isEmpty();
        }
        catch(SolrServerException ex) {
            throw new DocumentIndexingException(ex);
        }
    }
    
    @Override
    public void indexDocuments(List<String> documents, String revision) throws DocumentIndexingException {
        try {
            DocumentIndexingException joinedException = new DocumentIndexingException("Failed to index one or more documents");
            documents.stream().forEach((document) -> {
                try {
                    log.debug("Indexing: {}, revision: {}", document, revision);
                    solrServer.addBean(
                        indexGenerator.generateIndex(
                            reader.readBundle(document, revision)));
                }
                catch(Exception ex) {
                    log.error("Failed to index: {}", document, ex);
                    joinedException.addSuppressed(new DocumentIndexingException(
                        String.format("Failed to index %s : %s", document, ex.getMessage()), ex));
                    log.error("Suppressed indexing errors", (Object[]) joinedException.getSuppressed());
                }
            });
            solrServer.commit();
            
            //If an exception was supressed, then throw
            if(joinedException.getSuppressed().length != 0) {
                throw joinedException;
            }
        } catch(IOException | SolrServerException ex) {
            throw new DocumentIndexingException(ex);
        }
    }
    
    @Override
    public void unindexDocuments(List<String> documents) throws DocumentIndexingException {
        try {            
            solrServer.deleteById(documents);
            solrServer.commit();
        } catch (IOException | SolrServerException ex) {
            throw new DocumentIndexingException(ex);
        }
    }
}
