package uk.ac.ceh.gateway.catalogue.deims;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

@Profile("elter") // Only need this service running for elter catalogue instance
@Slf4j
@ToString
@Service
public class DeimsSolrQueryService {

    private static final String DEIMS = "deims";
    private final SolrClient solrClient;

    public DeimsSolrQueryService(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public List<DeimsSolrIndex> query(String term) throws SolrServerException {
        try {
            val query = new SolrQuery();
            query.setQuery(term);
            val response = solrClient.query(DEIMS, query, POST);
            return response.getBeans(DeimsSolrIndex.class);

        } catch (IOException | SolrServerException ex) {
            throw new SolrServerException(ex);
        }
    }
}
