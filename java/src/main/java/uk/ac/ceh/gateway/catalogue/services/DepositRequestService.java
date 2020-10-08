package uk.ac.ceh.gateway.catalogue.services;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DepositRequestDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import java.util.List;

@Service
@Slf4j
@ToString
public class DepositRequestService {
    private final DocumentRepository documentRepository;
    private final SolrClient solrServer;

    public DepositRequestService(DocumentRepository documentRepository, SolrClient solrServer) {
        this.documentRepository = documentRepository;
        this.solrServer = solrServer;
        log.info("Creating {}", this);
    }

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
        val finder = new SolrDocumentFinder<DepositRequestDocument>(solrServer, DepositRequestDocument.class);
        return finder.find(String.format("documentType:DEPOSIT_REQUEST_DOCUMENT AND view:%s", user.getUsername()));
    }

    public List<DepositRequestDocument> getAll() {
        val finder = new SolrDocumentFinder<DepositRequestDocument>(solrServer, DepositRequestDocument.class);
        return finder.find("documentType:DEPOSIT_REQUEST_DOCUMENT");
    }
}
