package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@Slf4j
@ToString
@Service
public class DEIMSSolrScheduledSiteService {

    private final RestTemplate restTemplate;
    private final SolrClient solrClient;
    private final URI address;
    private final int ONE_MINUTE = 60000;
    private final int SEVEN_DAYS = 604800000;
    private static final String DEIMS = "deims";

    public DEIMSSolrScheduledSiteService(@Qualifier("normal") RestTemplate restTemplate,
                                         SolrClient solrClient,
                                         @Value("${diems.sites}") String address) {
        this.restTemplate = restTemplate;
        this.solrClient = solrClient;
        this.address = UriComponentsBuilder.fromHttpUrl(address).build().toUri();
    }

    @Scheduled(initialDelay = ONE_MINUTE, fixedDelay = SEVEN_DAYS)
    protected void fetchDEIMSSitesAndAddToSolr() throws DocumentIndexingException {
        val response = restTemplate.getForEntity(
                this.address,
                DIEMSSite[].class
        );

        DIEMSSite[] sites = response.getBody();
        try {

            solrClient.deleteByQuery(DEIMS, "*:*");
            solrClient.addBean(DEIMS, sites);
            solrClient.commit();

        } catch (IOException | SolrServerException ex) {
            throw new DocumentIndexingException(ex);
        }
    }
}

@Data
class DIEMSSite {
    private String title;
    private id id;
    private String coordinates;
    private String changed;

    public DIEMSSite() {
    }

    public String getURL() {
        return this.getId().prefix + this.getId().suffix;
    }

    @Data
    private class id {
        private String prefix;
        private String suffix;

        public id() {
        }
    }
}


