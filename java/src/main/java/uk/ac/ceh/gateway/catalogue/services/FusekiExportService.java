package uk.ac.ceh.gateway.catalogue.services;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.TimeConstants;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.exports.CatalogueExportService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static uk.ac.ceh.gateway.catalogue.util.Headers.withBasicAuth;

@Profile("exports")
@Slf4j
@Service
@ToString
public class FusekiExportService implements CatalogueExportService {
    private final RestTemplate restTemplate;
    private final String baseUri;
    private final String fusekiUrl;
    private final String fusekiUsername;
    private final String fusekiPassword;
    private final String catalogueId;

    private final DocumentsToTurtleService documentsToTurtleService;

    public FusekiExportService(
        CatalogueService catalogueService,
        Configuration configuration,
        DocumentRepository documentRepository,
        MetadataListingService listing,
        @Qualifier("normal") RestTemplate restTemplate,
        @Value("${documents.baseUri}") String baseUri,
        @Value("${fuseki.url}/ds") String fusekiUrl,
        @Value("${fuseki.username}") String fusekiUsername,
        @Value("${fuseki.password}") String fusekiPassword
    ) {
        log.info("Creating");

        this.restTemplate = restTemplate;
        this.baseUri = baseUri;
        this.fusekiUrl = fusekiUrl;
        this.fusekiUsername = fusekiUsername;
        this.fusekiPassword = fusekiPassword;

        this.catalogueId = catalogueService.defaultCatalogue().getId();

        this.documentsToTurtleService = new CatalogueToTurtleService(
            catalogueService,
            documentRepository,
            configuration,
            listing,
            baseUri
        );
    }

    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE, fixedDelay = TimeConstants.ONE_DAY)
    @SneakyThrows
    public void runExport() {
        documentsToTurtleService.getBigTtl(catalogueId).ifPresent(ttl -> {
            post(ttl);
            log.info("Posted public metadata documents as ttl to {}", fusekiUrl);
        });
    }

    private void post(String data) {
        String graphName = baseUri; //this is from the first line after the prefixes in the big.ttl - which we've set to be baseUri that is injected into the template earlier in this code
        String serverUrl = fusekiUrl + "?graph=" + graphName;

        try {
            // PUT the data - this works if there's no graph and if there's an existing graph, in which case it's updated
            HttpHeaders headers = withBasicAuth(fusekiUsername, fusekiPassword);
            headers.add(HttpHeaders.CONTENT_TYPE, "text/turtle");
            HttpEntity<String> request = new HttpEntity<>(data, headers);
            restTemplate.put(serverUrl, request);
        } catch (RestClientResponseException ex) {
            log.error(
                "Error communicating with supplied URL: (statusCode={}, status={}, headers={}, body={})",
                ex.getRawStatusCode(),
                ex.getStatusText(),
                ex.getResponseHeaders(),
                ex.getResponseBodyAsString()
            );
            throw ex;
        }
    }
}
