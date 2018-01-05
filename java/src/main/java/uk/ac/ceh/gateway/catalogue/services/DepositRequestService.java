package uk.ac.ceh.gateway.catalogue.services;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.google.common.collect.Lists;

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
        SolrQuery query = new SolrQuery();
        query.setQuery(String.format("documentType:DEPOSIT_REQUEST_DOCUMENT AND view:%s", user.getUsername()));
        return find(query);
    }

    public List<DepositRequestDocument> getAll() {
        SolrQuery query = new SolrQuery();
        query.setQuery("documentType:DEPOSIT_REQUEST_DOCUMENT");
        return find(query);
    }

    private List<DepositRequestDocument> find(SolrQuery query) {
        List<DepositRequestDocument> found = Lists.newArrayList();
        try {
            QueryResponse qr = solrServer.query(query);
            val solrDocumentList = qr.getResults();
            for (val solrDocument : solrDocumentList) {
                String identifier = (String) solrDocument.getFieldValue("identifier");
                found.add(get(identifier));
            }
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
        return found;
    }
}
