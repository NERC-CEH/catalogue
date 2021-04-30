package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Profile("elter") // Only need this service running for elter catalogue instance
@Slf4j
@ToString(of="address")
@Service
public class DeimsSolrScheduledSiteService {

    private final RestTemplate restTemplate;
    private final SolrClient solrClient;
    private final String address;
    private final int ONE_MINUTE = 60000;
    private final int SEVEN_DAYS = 604800000;
    private static final String DEIMS = "deims";

    public DeimsSolrScheduledSiteService(
        @Qualifier("normal") RestTemplate restTemplate,
        SolrClient solrClient,
        @Value("${deims.sites}") String address
    ) {
        this.restTemplate = restTemplate;
        this.solrClient = solrClient;
        this.address = address;
        log.info("Creating {}", this);
    }

    @Scheduled(initialDelay = ONE_MINUTE, fixedDelay = SEVEN_DAYS)
    protected void fetchDEIMSSitesAndAddToSolr() throws DocumentIndexingException {
        log.info("Re-indexing DEIMS sites");
        val response = restTemplate.getForEntity(
                this.address,
                DeimsSite[].class
        );

        DeimsSite[] sites = response.getBody();
        log.info("Retrieved {} DEIMS sites", sites.length);
        try {

            solrClient.deleteByQuery(DEIMS, "*:*");
            for (DeimsSite site : sites) {
                solrClient.addBean(DEIMS, new DeimsSolrIndex(site));
                log.debug("Added {}, {}", site.getIdentifier(), site.getTitle());
            }
            solrClient.commit(DEIMS);

        } catch (IOException | SolrServerException ex) {
            log.error("Failed to re-index DEIMS sites");
            throw new DocumentIndexingException(ex);
        }
    }

    @Data
    static class DeimsSite {
        String title;
        Id id;
        String coordinates;
        String changed;

        public String getIdentifier() {
            return id.getSuffix();
        }

        public String getURL() {
            return this.getId().prefix + this.getId().suffix;
        }

        @Data
        private static class Id {
            String prefix;
            String suffix;
        }
    }

    @lombok.Value
    static class DeimsSolrIndex {
        String title;
        String id;
        String url;

        public DeimsSolrIndex(DeimsSite site) {
            this.title = site.getTitle();
            this.id = site.getIdentifier();
            this.url = site.getURL();
        }
    }
}


