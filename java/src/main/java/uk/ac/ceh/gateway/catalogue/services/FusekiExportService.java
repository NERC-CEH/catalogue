package uk.ac.ceh.gateway.catalogue.services;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.TimeConstants;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.exports.CatalogueExportService;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.util.*;
import java.util.stream.Collectors;

import static uk.ac.ceh.gateway.catalogue.util.Headers.withBasicAuth;

@Profile("exports")
@Slf4j
@Service
@ToString
public class FusekiExportService implements CatalogueExportService {
    private final Configuration configuration;
    private final DocumentRepository documentRepository;
    private final MetadataListingService listing;
    private final RestTemplate restTemplate;
    private final String baseUri;
    private final String fusekiUrl;
    private final String fusekiUsername;
    private final String fusekiPassword;

    private final String catalogueId;
    private final String catalogueTitle;

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

        this.configuration = configuration;
        this.documentRepository = documentRepository;
        this.listing = listing;
        this.restTemplate = restTemplate;
        this.baseUri = baseUri;
        this.fusekiUrl = fusekiUrl;
        this.fusekiUsername = fusekiUsername;
        this.fusekiPassword = fusekiPassword;

        Catalogue defaultCatalogue = catalogueService.defaultCatalogue();
        this.catalogueId = defaultCatalogue.getId();
        this.catalogueTitle = defaultCatalogue.getTitle();
    }

    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE, fixedDelay = TimeConstants.ONE_DAY)
    @SneakyThrows
    public void runExport() {
        post(getBigTtl());
        log.info("Posted public metadata documents as ttl to {}", fusekiUrl);
    }

    private String getBigTtl() {
        List<String> ids = getRequiredIds();
        String catalogueTtl = generateCatalogueTtl(getCatalogueModel(ids));
        List<String> recordsTtl = getRecordsTtl(ids);

        String bigTtl = catalogueTtl.concat(String.join("\n", recordsTtl));
        log.debug("Big turtle to send: ", bigTtl);
        return bigTtl;
    }

    private List<String> getRequiredIds(){
        List<String> ids = listing.getPublicDocumentsOfCatalogue(catalogueId);
        return ids.stream()
                .map(this::getMetadataDocument)
                .filter(this::isRequired)
                .map(MetadataDocument::getId)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public String generateCatalogueTtl(Map<String, Object> model){
        val freemarkerTemplate = configuration.getTemplate("rdf/catalogue.ttl.ftlh");
        return FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, model);
    }

    private Map<String, Object> getCatalogueModel(List<String> ids){
        Map<String, Object> model = new HashMap<>();
        model.put("records", ids);
        model.put("catalogue", catalogueId);
        model.put("title", catalogueTitle);
        model.put("baseUri", baseUri);
        return model;
    }

    private List<String> getRecordsTtl(List<String> ids){
        return ids.stream()
                .map(this::getMetadataDocument)
                .map(this::docToString)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private MetadataDocument getMetadataDocument(String id) {
        return documentRepository.read(id);
    }

    private boolean isRequired(MetadataDocument doc) {
        String[] requiredTypes = {"service","dataset"};
        return Arrays.asList(requiredTypes).contains(doc.getType());
    }

    @SneakyThrows
    public String docToString(MetadataDocument model){
        val freemarkerTemplate = configuration.getTemplate("rdf/ttlUnprefixed.ftlh");
        return FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, model);
    }

    private void post(String data){
        String graphName = baseUri; //this is from the first line after the prefixes in the big.ttl - which we've set to be baseUri that is injected into the template earlier in this code
        String serverUrl = new StringBuilder().append(fusekiUrl).append("?graph=").append(graphName).toString();

        try {
            HttpHeaders headers = withBasicAuth(fusekiUsername, fusekiPassword);
            headers.add(HttpHeaders.CONTENT_TYPE, "text/turtle");

            HttpEntity<String> request = new HttpEntity<>(data, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(serverUrl, request, String.class);
            log.info("Status code: {}", response.getStatusCode());
            log.info("Response {}", response);
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
