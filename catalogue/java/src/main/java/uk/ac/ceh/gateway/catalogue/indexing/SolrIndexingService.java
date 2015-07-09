package uk.ac.ceh.gateway.catalogue.indexing;

import java.io.IOException;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

/**
 * This is the Solr Indexing Service. Instances of this can read documents from
 * a DataRepository and index them with the supplied IndexGenerator. The indexes
 * will then go into an instance of Solr for speedy text based searches. 
 * @author cjohn
 * @param <D> type of documents to be read from the DataRepository
 */
public class SolrIndexingService<D> extends AbstractIndexingService<D, SolrIndex> {
    private final SolrServer solrServer;

    public SolrIndexingService(
            BundledReaderService<D> reader,
            DocumentListingService listingService,
            DataRepository<?> repo,
            IndexGenerator<D, SolrIndex> indexGenerator,
            SolrServer solrServer) {
        super(reader, listingService, repo, indexGenerator);
        this.solrServer = solrServer;
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
    public void unindexDocuments(List<String> documents) throws DocumentIndexingException {
        try {            
            solrServer.deleteById(documents);
            solrServer.commit();
        } catch (IOException | SolrServerException ex) {
            throw new DocumentIndexingException(ex);
        }
    }

    @Override
    public void clearIndex() throws DocumentIndexingException {
        try {
            solrServer.deleteByQuery("*:*");
        } catch (IOException | SolrServerException ex) {
            throw new DocumentIndexingException(ex);
        }
    }

    @Override
    public void index(SolrIndex toIndex) throws Exception {
        solrServer.addBean(toIndex);
    }

    @Override
    public void commit() throws DocumentIndexingException {
        try {
            solrServer.commit();
        } catch (IOException | SolrServerException ex) {
            throw new DocumentIndexingException(ex);
        }
    }
}
