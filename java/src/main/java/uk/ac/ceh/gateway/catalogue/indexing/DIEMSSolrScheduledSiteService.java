package uk.ac.ceh.gateway.catalogue.indexing;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
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

@Slf4j
@ToString
@Service
public class DIEMSSolrScheduledSiteService {
    private final DocumentIndexingService solrIndex;
    @Value("${solr.server.documents.url}") String solrDocumentServerUrl;
    @Value("${data.repository.location}") private String dataRepositoryLocation;
    private final RestTemplate restTemplate;
    private final URI address;

    public DIEMSSolrScheduledSiteService(
            @Qualifier("normal") RestTemplate restTemplate,
            @Qualifier("solr-index") DocumentIndexingService solrIndex
    ) {
        this.restTemplate = restTemplate;
        this.solrIndex = solrIndex;
        this.address = UriComponentsBuilder.fromHttpUrl("https://deims.org/api/sites").build().toUri();
    }

    @Scheduled(initialDelay = 0, fixedDelay = 604800000) // 604800000 milliseconds = 1 week
    protected void fetchDEIMS() throws SolrServerException, IOException {
        val headers = new HttpHeaders();
        headers.add("Authorization", "Bearer");

        val request = new HttpEntity<>(headers);
        val response = restTemplate.exchange(
                this.address,
                HttpMethod.GET,
                request,
                JsonNode.class
        );
        JsonNode output = response.getBody();

        SolrInputDocument doc = new SolrInputDocument();

       while (output.get("title").elements().hasNext() &&
               output.get("prefix").elements().hasNext() &&
               output.get("suffix").elements().hasNext()) {
           doc.addField((output.get("title").elements().next().asText()),
                   (output.get("prefix").elements().next().asText() +
                           output.get("suffix").elements().next().asText()));
           //(id is prefix and suffix joined)
       }

       createSolrCore(doc);
    }

    private void createSolrCore(SolrInputDocument siteData) throws SolrServerException, IOException {
        String coreName = "DIEMS";
        SolrClient client = new HttpSolrClient.Builder(solrDocumentServerUrl).build();
        CoreAdminRequest.Create createRequest = new CoreAdminRequest.Create();
        createRequest.setCoreName(coreName);
        createRequest.setInstanceDir(dataRepositoryLocation + "/diems");
        client.add(siteData);
        createRequest.process(client);
    }
}
