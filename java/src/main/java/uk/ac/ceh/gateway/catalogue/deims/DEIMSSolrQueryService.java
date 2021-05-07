package uk.ac.ceh.gateway.catalogue.deims;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;

import java.io.IOException;
import java.util.List;

@Profile("elter") // Only need this service running for elter catalogue instance
@Slf4j
@ToString
@Service
public class DEIMSSolrQueryService {

    private static final String DEIMS = "deims";
    private final SolrClient solrClient;

    public DEIMSSolrQueryService(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public List<DeimsSite> query(String value) throws SolrServerException {
        try {
            SolrQuery query = new SolrQuery();
            query.set(DEIMS, value);
            QueryResponse response = solrClient.query(query);
            return response.getBeans(DeimsSite.class);

        } catch (IOException | SolrServerException ex) {
            throw new SolrServerException(ex);
        }
    }
}
