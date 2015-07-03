package uk.ac.ceh.gateway.catalogue.linking;

import com.hp.hpl.jena.rdf.model.Model;
import java.io.IOException;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

@Data
@Slf4j
public class JenaDocumentLinkService<D> implements DocumentLinkService {
    private final DataRepository<CatalogueUser> repo;
    private final BundledReaderService<D> reader;
    private final DocumentListingService listingService;
    private final DocumentIdentifierService documentIdentifierService;
    private final LDExtractor<D> ldExtractor;
    
    private final Model model;

    @Override
    public boolean isEmpty() {
        return true;
    }
    
    @Override
    public void rebuildLinks() throws DocumentLinkingException {
        try {
            //linkDatabase.empty(); TODO Delete all documents
            String revision = repo.getLatestRevision().getRevisionID();
            linkDocuments(listingService.filterFilenames(repo.getFiles(revision)), revision);
        } catch (DataRepositoryException ex) {
            throw new DocumentLinkingException("Unable to get file names from Git", ex);
        }
    }

    @Override
    public void linkDocuments(List<String> documents, String revision) throws DocumentLinkingException {
        DocumentLinkingException joinedException = new DocumentLinkingException("Failed to link one or more documents");
        documents.stream().forEach((document) -> {
            try {
                log.debug("Indexing: {}, revision: {}", document, revision);
                model.add(
                    ldExtractor.getStatements(
                        reader.readBundle(document, revision)));
            }
            catch(Exception ex) {
                log.error("Failed to link: {}", document, ex);
                joinedException.addSuppressed(new DocumentLinkingException(
                    String.format("Failed to link %s : %s", document, ex.getMessage()), ex));
                log.error("Suppressed linking errors", (Object[]) joinedException.getSuppressed());
            }
        });
        //solrServer.commit();

        //If an exception was supressed, then throw
        if(joinedException.getSuppressed().length != 0) {
            throw joinedException;
        }
    }

    @Override
    public void unlinkDocuments(List<String> fileIdentifiers) throws DocumentLinkingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}