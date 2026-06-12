package uk.ac.ceh.gateway.catalogue.services;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes;
import uk.ac.ceh.gateway.catalogue.TimeConstants;
import uk.ac.ceh.gateway.catalogue.exports.CatalogueExportService;
import uk.ac.ceh.gateway.catalogue.exports.DocumentsToTurtleService;
import uk.ac.ceh.gateway.catalogue.wellknown.VoidStats;
import uk.ac.ceh.gateway.catalogue.wellknown.VoidStatsService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final List<String> catalogueIds;
    private final DocumentsToTurtleService documentsToTurtleService;
    private final VoidStatsService voidStatsService;
    private final MetadataListingService metadataListingService;

    public FusekiExportService(
        DocumentsToTurtleService documentsToTurtleService,
        @Qualifier("normal") RestTemplate restTemplate,
        @Value("${documents.baseUri}") String baseUri,
        @Value("#{'${fuseki.catalogueIds}'.split(',')}") List<String> catalogueIds,
        @Value("${fuseki.datasetUrl}") String fusekiUrl,
        @Value("${fuseki.username}") String fusekiUsername,
        @Value("${fuseki.password}") String fusekiPassword,
        VoidStatsService voidStatsService,
        MetadataListingService metadataListingService
    ) {
        log.info("Creating");

        this.restTemplate = restTemplate;
        this.baseUri = baseUri;
        this.fusekiUrl = fusekiUrl;
        this.fusekiUsername = fusekiUsername;
        this.fusekiPassword = fusekiPassword;
        this.catalogueIds = catalogueIds;
        this.documentsToTurtleService = documentsToTurtleService;
        this.voidStatsService = voidStatsService;
        this.metadataListingService = metadataListingService;
    }

    private record TurtleStats(long triples, Map<String, Long> classEntityCounts) {}

    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE, fixedDelay = TimeConstants.ONE_DAY)
    public void runExport() {
        log.info("Running Fuseki export");
        Map<String, String> catalogueTtls = new LinkedHashMap<>();
        catalogueIds.forEach(id ->
            documentsToTurtleService.getBigTtl(id).ifPresent(ttl -> catalogueTtls.put(id, ttl))
        );
        if (catalogueTtls.isEmpty()) {
            log.info("No documents to export");
            return;
        }
        post(String.join("\n", catalogueTtls.values()));
        log.info("Posted public metadata documents as ttl to {}", fusekiUrl);
        catalogueIds.stream()
            .filter(id -> !catalogueTtls.containsKey(id))
            .forEach(voidStatsService::remove);
        catalogueTtls.forEach((id, ttl) -> {
            TurtleStats ts = parseTurtleStats(ttl);
            voidStatsService.update(id, new VoidStats(
                metadataListingService.getPublicDocumentsOfCatalogue(id).size(),
                ts.triples(),
                ts.classEntityCounts()
            ));
        });
    }

    private TurtleStats parseTurtleStats(String ttl) {
        Model model = ModelFactory.createDefaultModel();
        try (InputStream is = new ByteArrayInputStream(ttl.getBytes(StandardCharsets.UTF_8))) {
            RDFDataMgr.read(model, is, Lang.TURTLE);
        } catch (Exception e) {
            log.warn("Failed to parse Turtle for stats: {}", e.getMessage());
            return new TurtleStats(0L, Map.of());
        }
        Map<String, Long> classEntityCounts = model.listStatements(null, RDF.type, (RDFNode) null)
            .toList()
            .stream()
            .filter(stmt -> stmt.getObject().isURIResource())
            .collect(Collectors.groupingBy(
                stmt -> stmt.getObject().asResource().getURI(),
                Collectors.counting()
            ));
        return new TurtleStats(model.size(), Map.copyOf(classEntityCounts));
    }

    private void post(String data) {
        String serverUrl = fusekiUrl + "?graph=" + baseUri;

        try {
            // PUT the data - this works if there's no graph and if there's an existing graph, in which case it's updated
            HttpHeaders headers = withBasicAuth(fusekiUsername, fusekiPassword);
            headers.setContentType(new MediaType(CatalogueMediaTypes.RDF_TTL, StandardCharsets.UTF_8));
            HttpEntity<String> request = new HttpEntity<>(data, headers);
            restTemplate.put(serverUrl, request);
        } catch (RestClientResponseException ex) {
            log.error(
                "Error communicating with supplied URL: (statusCode={}, status={}, headers={}, body={})",
                ex.getStatusCode().value(),
                ex.getStatusText(),
                ex.getResponseHeaders(),
                ex.getResponseBodyAsString()
            );
            throw ex;
        }
    }
}
