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
import uk.ac.ceh.gateway.catalogue.exports.DescriptionCache;
import uk.ac.ceh.gateway.catalogue.exports.DocumentsToTurtleService;
import uk.ac.ceh.gateway.catalogue.exports.SourceGraphProvider;
import uk.ac.ceh.gateway.catalogue.wellknown.VoidStats;
import uk.ac.ceh.gateway.catalogue.wellknown.VoidStatsService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
    private final List<SourceGraphProvider> sourceGraphProviders;
    private final DescriptionCache descriptionCache;
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
        List<SourceGraphProvider> sourceGraphProviders,
        DescriptionCache descriptionCache
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
        this.sourceGraphProviders = sourceGraphProviders;
        this.descriptionCache = descriptionCache;
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

        // One pass per catalogue. Both things the parse is needed for -- the IRIs
        // the source graphs describe, and the VoID stats -- are taken from each
        // model before the next is read, so it can be collected rather than held.
        // Holding all of them across postSourceGraphs below meant every
        // catalogue's graph stayed reachable for the length of that method's
        // network phase, which is thousands of requests and minutes of wall
        // clock. A Jena in-memory model runs well above the size of the Turtle
        // it was read from, and the heap this runs on is capped at 750Mi.
        Set<String> referencedIris = new HashSet<>();
        Map<String, TurtleStats> statsByCatalogue = new LinkedHashMap<>();
        List<String> unparseable = new ArrayList<>();
        catalogueTtls.forEach((id, ttl) -> {
            Optional<Model> parsed = parse(id, ttl);
            if (parsed.isEmpty()) {
                unparseable.add(id);
                return;
            }
            Model model = parsed.get();
            collectReferencedIris(model, referencedIris);
            statsByCatalogue.put(id, turtleStats(model));
            model.close();
        });

        try {
            if (unparseable.isEmpty()) {
                postSourceGraphs(referencedIris);
            } else {
                // Every source graph describes only what the catalogue's graph
                // cites, and each is written with a PUT that replaces it. A
                // catalogue that did not parse contributes none of its IRIs, so
                // going ahead would replace each graph with one describing fewer
                // entities -- and no provider can notice, because the
                // publish-whole-or-not-at-all guard measures completeness against
                // this set, which is already the shortened one. Before the source
                // graphs existed a parse failure here cost only the VoID stats.
                log.warn(
                    "Not publishing any source graph: the Turtle for {} could not be parsed, so "
                        + "the referenced IRIs are incomplete and every graph would be replaced "
                        + "with less than it already holds",
                    unparseable);
            }
        } finally {
            // One snapshot for the whole run. Every retriever used to write the
            // entire cache to the share whenever it had fetched something, which
            // is ten whole-file rewrites across the authorities of phases 2 to 5.
            // In a finally so that a provider failing in a way postSourceGraphs
            // does not catch still leaves the run's fetches recoverable.
            descriptionCache.save();
        }

        catalogueIds.stream()
            .filter(id -> !catalogueTtls.containsKey(id))
            .forEach(voidStatsService::remove);
        // A catalogue that did not parse keeps the stats it already had. They are
        // stale by a run; the zeroes this used to publish for it were simply
        // wrong, and a VoID description claiming a dataset holds no triples is a
        // worse answer than yesterday's count.
        statsByCatalogue.forEach((id, ts) ->
            voidStatsService.update(id, new VoidStats(
                metadataListingService.getPublicDocumentsOfCatalogue(id).size(),
                ts.triples(),
                ts.classEntityCounts()
            )));
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
    private void postSourceGraphs(Set<String> referencedIris) {
        for (SourceGraphProvider provider : sourceGraphProviders) {
            Map<String, String> graphs;
            try {
                graphs = provider.graphs(referencedIris);
            } catch (Exception ex) {
                // One provider failing must not cost the others, any more than
                // one graph failing costs the catalogue's.
                log.warn("Could not build source graphs from {}: {}",
                    provider.getClass().getSimpleName(), ex.getMessage());
                continue;
            }
            graphs.forEach((graph, ttl) -> {
                try {
                    post(graph, ttl);
                    log.info("Posted source graph {}", graph);
                } catch (Exception ex) {
                    log.warn("Could not post source graph {}: {}", graph, ex.getMessage());
                }
            });
        }
    }

    @Override
    public Date getLastExported() {
        Date exported = this.lastExported;
        return exported == null ? null : new Date(exported.getTime());
    }

    /**
     * @return the parsed graph, or empty if the Turtle could not be read.
     *         Distinguishing the two matters: this used to return an empty model
     *         on failure, which is indistinguishable from a catalogue that holds
     *         nothing, and the caller cannot make the right decision without
     *         knowing which it had.
     */
    private Optional<Model> parse(String catalogueId, String ttl) {
        Model model = ModelFactory.createDefaultModel();
        try (InputStream is = new ByteArrayInputStream(ttl.getBytes(StandardCharsets.UTF_8))) {
            RDFDataMgr.read(model, is, Lang.TURTLE);
        } catch (Exception e) {
            log.warn("Failed to parse the exported Turtle for {}: {}", catalogueId, e.getMessage());
            return Optional.empty();
        }
        return Optional.of(model);
    }

    /**
     * Adds every IRI this catalogue's graph refers to. The source graphs describe
     * only entities something actually cites, so this is the input to that:
     * objects rather than subjects, since a subject in this graph is one of our
     * own records or a node we minted.
     *
     * <p>Accumulates into the caller's set rather than returning one per model,
     * so each model can be released as soon as it has been read.
     */
    private static void collectReferencedIris(Model model, Set<String> into) {
        model.listObjects().forEachRemaining(object -> {
            if (object.isURIResource()) {
                into.add(object.asResource().getURI());
            }
        });
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
