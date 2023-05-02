package uk.ac.ceh.gateway.catalogue.deims;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient;
import org.apache.solr.common.params.CommonParams;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

@Profile("server:elter")
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
            query.setParam(CommonParams.DF, "title");
            query.setSort("title", SolrQuery.ORDER.asc);
            query.setRows(100);
            val response = solrClient.query(DEIMS, query, POST);
            return response.getBeans(DeimsSolrIndex.class);

        } catch (IOException | SolrServerException | BaseHttpSolrClient.RemoteSolrException ex) {
            throw new SolrServerException(ex);
        }
    }
}
