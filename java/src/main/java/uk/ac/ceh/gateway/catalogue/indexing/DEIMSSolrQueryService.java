package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.stereotype.Service;

@Slf4j
@ToString
@Service
public class DEIMSSolrQueryService {
    private final SolrClient solrClient;

    public DEIMSSolrQueryService(SolrClient solrClient) {
        this.solrClient = solrClient;
    }
}
