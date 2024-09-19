package uk.ac.ceh.gateway.catalogue.indexing.solr;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.AbstractIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreement;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is the Solr Indexing Service. Instances of this can read documents from
 * a DataRepository and index them with the supplied IndexGenerator. The indexes
 * will then go into an instance of Solr for speedy text based searches.
 */
@Slf4j
@ToString(callSuper = true)
public class SolrIndexingService extends AbstractIndexingService<MetadataDocument, SolrIndex> {
    private final SolrClient solrClient;
    private final JenaLookupService lookupService;
    private final DocumentIdentifierService identifierService;
    public static final String DOCUMENTS = "documents";

    private static final Set<String> UNINDEXED_RESOURCE_STATUS = Set.of("Deleted");

    public SolrIndexingService(
            BundledReaderService<MetadataDocument> reader,
            DocumentListingService listingService,
            DataRepository<CatalogueUser> repo,
            IndexGenerator<MetadataDocument, SolrIndex> indexGenerator,
            SolrClient solrClient,
            JenaLookupService lookupService,
            DocumentIdentifierService identifierService
    ) {
        super(reader, listingService, repo, indexGenerator);
        this.solrClient = solrClient;
        this.lookupService = lookupService;
        this.identifierService = identifierService;
        log.info("Creating {}", this);
    }

    @SneakyThrows
    @Override
    public boolean isIndexEmpty() throws DocumentIndexingException {
        try {
            return solrClient.query(DOCUMENTS, new SolrQuery("*:*")).getResults().isEmpty();
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

    @SneakyThrows
    @Override
    protected boolean canIndex(MetadataDocument doc) {
        if (doc == null) {
            return false;
        }
        if (doc instanceof GeminiDocument gemini) {
            if (UNINDEXED_RESOURCE_STATUS.contains(gemini.getResourceStatus())) {
                unindexDocuments(List.of(gemini.getId())); // Needed to remove existing superseded or deleted record from Solr
                return false;
            } else {
                return true;
            }
        }
        return !(doc instanceof ServiceAgreement);
    }

    @Override
    public void unindexDocuments(List<String> documents) throws DocumentIndexingException {
        try {
            solrClient.deleteById(DOCUMENTS, documents);
            commit();
        } catch (IOException | SolrServerException ex) {
            throw new DocumentIndexingException(ex);
        }
    }

    @Override
    protected void clearIndex() throws DocumentIndexingException {
        try {
            solrClient.deleteByQuery(DOCUMENTS,"*:*");
        } catch (IOException | SolrServerException ex) {
            throw new DocumentIndexingException(ex);
        }
    }

    @Override
    protected void index(SolrIndex toIndex) throws Exception {
        solrClient.addBean(DOCUMENTS, toIndex);
    }

    @Override
    protected MetadataDocument readDocument(String document, String revision) {
        return readDocument(document);
    }

    private void commit() throws DocumentIndexingException {
        try {
            solrClient.commit(DOCUMENTS);
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
