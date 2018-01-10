package uk.ac.ceh.gateway.catalogue.services;

import java.util.List;
import org.apache.solr.client.solrj.SolrServer;
import lombok.AllArgsConstructor;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DepositRequestDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@AllArgsConstructor
public class DepositRequestService {

    private final DocumentRepository documentRepository;
    private final SolrServer solrServer;

    public void save(CatalogueUser user, DepositRequestDocument depositRequest) {
        try {
            documentRepository.saveNew(user, depositRequest, "DEPOSIT_REQUEST_DOCUMENT", "new deposit request");
        } catch(DocumentRepositoryException err) {
            throw new RuntimeException(err);
        }
    }

    public DepositRequestDocument get(String guid) {
        try {
            val document = documentRepository.read(guid);
            return (DepositRequestDocument) document;
        } catch(DocumentRepositoryException err) {
            throw new RuntimeException(err);
        }
    }

    public List<DepositRequestDocument> getForUser(CatalogueUser user) {
        val finder = new SolrDocumentFinder<DepositRequestDocument>(solrServer, documentRepository);
        return finder.find(String.format("documentType:DEPOSIT_REQUEST_DOCUMENT AND view:%s", user.getUsername()));
    }

    public List<DepositRequestDocument> getAll() {
        val finder = new SolrDocumentFinder<DepositRequestDocument>(solrServer, documentRepository);
        return finder.find("documentType:DEPOSIT_REQUEST_DOCUMENT");
    }
}
