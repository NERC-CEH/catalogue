package uk.ac.ceh.gateway.catalogue.serviceagreement;

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

@Profile("service-agreement")
@Slf4j
@Service
public class SolrServiceAgreementSearch implements ServiceAgreementSearch {

    private static final String SERVICE_AGREEMENT = "service-agreement";
    private final SolrClient solrClient;

    public SolrServiceAgreementSearch(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    @Override
    public List<ServiceAgreementSolrIndex> query(String term) throws RuntimeException {
        try {
            val query = new SolrQuery();
            query.setQuery(term);
            query.setParam(CommonParams.DF, "title");
            query.setSort("title", SolrQuery.ORDER.asc);
            query.setRows(100);
            val response = solrClient.query(SERVICE_AGREEMENT, query, POST);
            return response.getBeans(ServiceAgreementSolrIndex.class);

        } catch (IOException | SolrServerException | BaseHttpSolrClient.RemoteSolrException ex) {
            throw new ServiceAgreementException(ex);
        }
    }
}
