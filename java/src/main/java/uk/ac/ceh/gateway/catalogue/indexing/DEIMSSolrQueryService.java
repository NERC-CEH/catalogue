package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@ToString
@Service
public class DEIMSSolrQueryService {

    private static final String DEIMS = "deims";
    private final SolrClient solrClient;

    public DEIMSSolrQueryService(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public List<String> query(String name, String value) throws DocumentIndexingException {
        try {
            SolrQuery query = new SolrQuery();
            query.set(name, value);
            QueryResponse response = solrClient.query(query);
            return response.getBeans(String.class);

        } catch (IOException | SolrServerException ex) {
            throw new DocumentIndexingException(ex);
        }
    }
}
