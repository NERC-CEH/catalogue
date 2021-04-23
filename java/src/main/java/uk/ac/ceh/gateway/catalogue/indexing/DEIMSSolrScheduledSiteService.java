package uk.ac.ceh.gateway.catalogue.indexing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@ToString
@Service
public class DEIMSSolrScheduledSiteService {

    private final RestTemplate restTemplate;
    private final SolrClient solrClient;
    private final URI address;

    public DEIMSSolrScheduledSiteService(@Qualifier("normal") RestTemplate restTemplate,
                                         @Qualifier("diems") SolrClient solrClient,
                                         @Value("${diems.sites}") String address) {
        this.restTemplate = restTemplate;
        this.solrClient = solrClient;
        this.address = UriComponentsBuilder.fromHttpUrl(address).build().toUri();
    }

    @Scheduled(initialDelay = 0, fixedDelay = 604800000) // 604800000 milliseconds = 1 week
    protected void fetchDEIMSSitesAndAddToSolr() throws DocumentIndexingException {
        val headers = new HttpHeaders();
        headers.add("Authorization", "Bearer");

        val request = new HttpEntity<>(headers);
        val response = restTemplate.exchange(
                this.address,
                HttpMethod.GET,
                request,
                DIEMSSite[].class
        );
        DIEMSSite[] sites = response.getBody();

        SolrInputDocument doc = new SolrInputDocument();

        for (DIEMSSite site : sites) {
            doc.addField(site.getTitle(), site.getURL());
        }

        try {

            solrClient.deleteByQuery("*:*");
            solrClient.addBean(doc);
            solrClient.commit();

        } catch (IOException | SolrServerException ex) {
            throw new DocumentIndexingException(ex);
        }
    }
}

class DIEMSSite {
    private String title;
    private id id;
    private String coordinates;
    private String changed;

    public DIEMSSite() {
    }

    public String getTitle() {
        return title;
    }

    public String getURL() {
        return this.getId().getPrefix() + this.getId().getSuffix();
    }

    public id getId() {
        return id;
    }

    private class id {
        private String prefix;
        private String suffix;

        public id() {
        }

        public String getPrefix() {
            return prefix;
        }

        public String getSuffix() {
            return suffix;
        }

    }

}

class DIEMSSiteList {
    private List<DIEMSSite> diemsSites;

    public DIEMSSiteList() {
        diemsSites = new ArrayList<>();
    }

    public List<DIEMSSite> getDiemsSites() {
        return diemsSites;
    }

}


