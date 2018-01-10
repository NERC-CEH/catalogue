package uk.ac.ceh.gateway.catalogue.services;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

@AllArgsConstructor
public class SolrDocumentFinder<T> {

    private final SolrServer solrServer;
    private final DocumentRepository documentRepository;

    public List<T> find(String query) {
        List<T> found = Lists.newArrayList();
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(query);
        try {
            QueryResponse qr = solrServer.query(solrQuery);
            val solrDocumentList = qr.getResults();
            for (val solrDocument : solrDocumentList) {
                String guid = (String) solrDocument.getFieldValue("identifier");
                @SuppressWarnings("unchecked")
                val document = (T) documentRepository.read(guid);
                found.add(document);
            }
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
        return found;
    }

}