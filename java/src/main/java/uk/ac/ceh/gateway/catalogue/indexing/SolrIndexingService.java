package uk.ac.ceh.gateway.catalogue.indexing;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.services.JenaLookupService;

/**
 * This is the Solr Indexing Service. Instances of this can read documents from
 * a DataRepository and index them with the supplied IndexGenerator. The indexes
 * will then go into an instance of Solr for speedy text based searches. 
 * @author cjohn
 * @param <D> type of documents to be read from the DataRepository
 */
public class SolrIndexingService<D> extends AbstractIndexingService<D, SolrIndex> {
    private final SolrServer solrServer;
    private final JenaLookupService lookupService;
    private final DocumentIdentifierService identifierService;

    public SolrIndexingService(
            BundledReaderService<D> reader,
            DocumentListingService listingService,
            DataRepository<?> repo,
            IndexGenerator<D, SolrIndex> indexGenerator,
            SolrServer solrServer,
            JenaLookupService lookupService,
            DocumentIdentifierService identifierService
    ) {
        super(reader, listingService, repo, indexGenerator);
        this.solrServer = solrServer;
        this.lookupService = lookupService;
        this.identifierService = identifierService;
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
            super.indexDocuments(documents, revision);
            super.indexDocuments(linkedDocuments(documents), revision); // reindex LinkDocuments
        } finally {
            commit();
        }
    }
        
    @Override
    public void unindexDocuments(List<String> documents) throws DocumentIndexingException {
        try {            
            solrServer.deleteById(documents);
            commit();
        } catch (IOException | SolrServerException ex) {
            throw new DocumentIndexingException(ex);
        }
    }

    @Override
    protected void clearIndex() throws DocumentIndexingException {
        try {
            solrServer.deleteByQuery("*:*");
        } catch (IOException | SolrServerException ex) {
            throw new DocumentIndexingException(ex);
        }
    }

    @Override
    protected void index(SolrIndex toIndex) throws Exception {
        solrServer.addBean(toIndex);
    }
    
    @Override
    protected D readDocument(String document, String revision) throws Exception {
        return readDocument(document);
    }
    
    private void commit() throws DocumentIndexingException {
        try {
            solrServer.commit();
        } catch (IOException | SolrServerException ex) {
            throw new DocumentIndexingException(ex);
        }
    }
    
    private List<String> linkedDocuments(List<String> documents) {
        return documents
            .stream()
            .flatMap(document -> lookupService.linked(identifierService.generateUri(document)).stream())
            .collect(Collectors.toList());
    }
}
