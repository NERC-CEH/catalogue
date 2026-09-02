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
import uk.ac.ceh.gateway.catalogue.exports.VocabularyGraphService;
import uk.ac.ceh.gateway.catalogue.wellknown.VoidStats;
import uk.ac.ceh.gateway.catalogue.wellknown.VoidStatsService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
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
    private final VocabularyGraphService vocabularyGraphService;
    private volatile Date lastExported;

    public FusekiExportService(
        DocumentsToTurtleService documentsToTurtleService,
        @Qualifier("normal") RestTemplate restTemplate,
        @Value("${documents.baseUri}") String baseUri,
        @Value("#{'${fuseki.catalogueIds}'.split(',')}") List<String> catalogueIds,
        @Value("${fuseki.datasetUrl}") String fusekiUrl,
        @Value("${fuseki.username}") String fusekiUsername,
        @Value("${fuseki.password}") String fusekiPassword,
        VoidStatsService voidStatsService,
        MetadataListingService metadataListingService,
        VocabularyGraphService vocabularyGraphService
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
        this.vocabularyGraphService = vocabularyGraphService;
    }

    private record TurtleStats(long triples, Map<String, Long> classEntityCounts) {}

    /**
     * Refreshes the prefetched Turtle cache (e.g. eidc) before every export, scheduled or manual,
     * so this never publishes a stale prefetched payload. See
     * {@link uk.ac.ceh.gateway.catalogue.exports.CatalogueToTurtleService#refresh()}.
     */
    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE, fixedDelay = TimeConstants.ONE_DAY)
    public void runExport() {
        log.info("Running Fuseki export");
        documentsToTurtleService.refresh();
        Map<String, String> catalogueTtls = new LinkedHashMap<>();
        catalogueIds.forEach(id ->
            documentsToTurtleService.getBigTtl(id).ifPresent(ttl -> catalogueTtls.put(id, ttl))
        );
        if (catalogueTtls.isEmpty()) {
            log.info("No documents to export");
            return;
        }
        post(baseUri, String.join("\n", catalogueTtls.values()));
        log.info("Posted public metadata documents as ttl to {}", fusekiUrl);

        // Parsed once and used twice: the VoID stats below, and the set of
        // external concepts the vocabulary graphs describe. Parsing 20MB of
        // Turtle a second time to answer the second question would be wasteful.
        Map<String, Model> parsed = new LinkedHashMap<>();
        catalogueTtls.forEach((id, ttl) -> parsed.put(id, parse(ttl)));

        postVocabularyGraphs(referencedIris(parsed.values()));

        catalogueIds.stream()
            .filter(id -> !catalogueTtls.containsKey(id))
            .forEach(voidStatsService::remove);
        parsed.forEach((id, model) -> {
            TurtleStats ts = turtleStats(model);
            voidStatsService.update(id, new VoidStats(
                metadataListingService.getPublicDocumentsOfCatalogue(id).size(),
                ts.triples(),
                ts.classEntityCounts()
            ));
        });
        lastExported = new Date();
    }

    /**
     * Publishes the vocabulary labels the application already holds, one named
     * graph per authority (dri-one #350 phase 1).
     *
     * <p>Each graph is written in its own try/catch and none of them can fail
     * the export. The PUT is all-or-nothing per graph, which is exactly how one
     * malformed literal in one grant number held back all 234,000 catalogue
     * triples for a week (dri-one #344); separating the writes means an
     * unavailable vocabulary, or a bad label in one of them, cannot stop the
     * catalogue graph from updating — or stop the other vocabularies publishing.
     */
    private void postVocabularyGraphs(Set<String> referencedIris) {
        Map<String, String> graphs;
        try {
            graphs = vocabularyGraphService.graphs(referencedIris);
        } catch (Exception ex) {
            log.warn("Could not build the vocabulary label graphs, skipping them: {}", ex.getMessage());
            return;
        }
        graphs.forEach((graph, ttl) -> {
            try {
                post(graph, ttl);
                log.info("Posted vocabulary labels to graph {}", graph);
            } catch (Exception ex) {
                log.warn("Could not post vocabulary labels to graph {}: {}", graph, ex.getMessage());
            }
        });
    }

    @Override
    public Date getLastExported() {
        Date exported = this.lastExported;
        return exported == null ? null : new Date(exported.getTime());
    }

    private Model parse(String ttl) {
        Model model = ModelFactory.createDefaultModel();
        try (InputStream is = new ByteArrayInputStream(ttl.getBytes(StandardCharsets.UTF_8))) {
            RDFDataMgr.read(model, is, Lang.TURTLE);
        } catch (Exception e) {
            log.warn("Failed to parse exported Turtle: {}", e.getMessage());
            return ModelFactory.createDefaultModel();
        }
        return model;
    }

    /**
     * Every IRI the catalogue's graph refers to. The vocabulary graphs describe
     * only concepts something actually cites, so this is the input to that:
     * objects rather than subjects, since a subject in this graph is one of our
     * own records or a node we minted.
     */
    private static Set<String> referencedIris(Collection<Model> models) {
        Set<String> iris = new HashSet<>();
        models.forEach(model -> model.listObjects().forEachRemaining(object -> {
            if (object.isURIResource()) {
                iris.add(object.asResource().getURI());
            }
        }));
        return iris;
    }

    private TurtleStats turtleStats(Model model) {
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

    private void post(String graph, String data) {
        String serverUrl = fusekiUrl + "?graph=" + graph;

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
