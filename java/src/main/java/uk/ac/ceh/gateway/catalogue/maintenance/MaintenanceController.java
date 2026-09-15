package uk.ac.ceh.gateway.catalogue.maintenance;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.controllers.DocumentController;
import uk.ac.ceh.gateway.catalogue.exports.CatalogueExportService;
import uk.ac.ceh.gateway.catalogue.exports.SourceGraphProgress;
import uk.ac.ceh.gateway.catalogue.exports.SourceGraphProvider;
import uk.ac.ceh.gateway.catalogue.exports.SourceGraphProvider.SourceGraph;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.mapserver.MapServerIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.solr.PendingEmbeddingService;
import uk.ac.ceh.gateway.catalogue.maintenance.MaintenanceResponse.GraphProgress;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@ToString
@Controller
@RequestMapping("maintenance")
@Secured(DocumentController.MAINTENANCE_ROLE)
public class MaintenanceController {
    private final DataRepositoryOptimizingService repoService;
    private final DocumentIndexingService solrIndex;
    private final DocumentIndexingService linkingService;
    private final MapServerIndexingService mapserverService;
    private final Optional<CatalogueExportService> catalogueExportService;
    private final List<SourceGraphProvider> sourceGraphProviders;
    private final Optional<SourceGraphProgress> sourceGraphProgress;
    /** Absent without the "vector-search" profile, which keeps the panel off the page. */
    private final Optional<PendingEmbeddingService> embeddingService;

    /** What a graph that no run has reached since startup is shown as. */
    static final String NOT_RUN = "Not run yet";

    /**
     * {@code CatalogueExportService} only exists under the {@code exports} profile, but this controller
     * is not profile-gated - every catalogue context (with or without {@code exports}) builds a
     * {@code MaintenanceController}. A plain constructor dependency on {@code CatalogueExportService}
     * would therefore stop any non-{@code exports} context from starting. {@code Optional} lets Spring
     * inject an empty value when the bean is absent instead of failing to wire the controller at all.
     */
    @Autowired
    public MaintenanceController(
        DataRepositoryOptimizingService repoService,
        @Qualifier("solr-index") DocumentIndexingService solrIndex,
        @Qualifier("jena-index") DocumentIndexingService linkingService,
        @Qualifier("mapserver-index") DocumentIndexingService mapserverService,
        Optional<CatalogueExportService> catalogueExportService,
        ObjectProvider<SourceGraphProvider> sourceGraphProviders,
        Optional<SourceGraphProgress> sourceGraphProgress,
        Optional<PendingEmbeddingService> embeddingService
    ) {
        this(repoService, solrIndex, linkingService, mapserverService, catalogueExportService,
            sourceGraphProviders.orderedStream().toList(), sourceGraphProgress, embeddingService);
    }

    /**
     * Package-private, so a test can supply providers directly. Spring reaches
     * this through the constructor above, which takes an {@code ObjectProvider}
     * rather than the list: a plain {@code List<SourceGraphProvider>} parameter
     * is a <em>required</em> dependency, so a context without the
     * {@code exports} profile - which has no providers at all - would fail to
     * start rather than inject an empty list.
     */
    MaintenanceController(
        DataRepositoryOptimizingService repoService,
        DocumentIndexingService solrIndex,
        DocumentIndexingService linkingService,
        DocumentIndexingService mapserverService,
        Optional<CatalogueExportService> catalogueExportService,
        List<SourceGraphProvider> sourceGraphProviders,
        Optional<SourceGraphProgress> sourceGraphProgress,
        Optional<PendingEmbeddingService> embeddingService
    ) {
        this.repoService = repoService;
        this.solrIndex = solrIndex;
        this.linkingService = linkingService;
        this.mapserverService = (MapServerIndexingService) mapserverService;
        this.catalogueExportService = catalogueExportService;
        this.sourceGraphProviders = List.copyOf(sourceGraphProviders);
        this.sourceGraphProgress = sourceGraphProgress;
        this.embeddingService = embeddingService;
        log.info("Creating");
    }

    @RequestMapping (method = RequestMethod.GET)
    @ResponseBody
    public MaintenanceResponse loadMaintenancePage() {
        MaintenanceResponse toReturn = new MaintenanceResponse();
        toReturn.setIndexedMapFilesCount(mapserverService.getIndexedFiles().size());
        try {
            toReturn.setLinked(!linkingService.isIndexEmpty());
        } catch(DocumentIndexingException ex) {
            toReturn.addMessage(ex.getMessage());
        }
        try {
            toReturn.setIndexed(!solrIndex.isIndexEmpty());
        } catch(DocumentIndexingException ex) {
            toReturn.addMessage(ex.getMessage());
        }
        try {
            toReturn.setHasMapFiles(!mapserverService.isIndexEmpty());
        } catch(DocumentIndexingException ex) {
            toReturn.addMessage(ex.getMessage());
        }
        try {
            toReturn.setLatestRevision(repoService.getLatestRevision());
        } catch(DataRepositoryException dre) {
            toReturn.addMessage(dre.getMessage());
        }
        toReturn.setLastOptimized(repoService.getLastOptimized());
        toReturn.setExportsAvailable(catalogueExportService.isPresent());
        catalogueExportService.ifPresent(service -> toReturn.setLastExported(service.getLastExported()));
        toReturn.setSourceGraphProgress(graphProgress());
        // Solr being unreachable is reported the same way the indexing checks above report it,
        // rather than taking the whole maintenance page down with it.
        embeddingService.ifPresent(service -> {
            try {
                val coverage = service.coverage();
                toReturn.setEmbeddingProgress(MaintenanceResponse.EmbeddingProgress.from(
                    coverage.embedded(), coverage.total(), coverage.pending(), coverage.abandoned()));
            } catch (Exception e) {
                toReturn.addMessage(e.getMessage());
            }
        });
        return toReturn;
    }

    /**
     * How far each authority's graph has got towards being published.
     *
     * <p>Assembled from two halves because neither knows the other: the
     * providers declare which graphs exist and what to call them, and
     * {@link SourceGraphProgress} records what the last run did to them but is
     * keyed only by graph URI. Joining here rather than in a service of its own
     * is not just convenience - the providers depend on {@code
     * SourceGraphProgress}, so a service holding both would close a cycle.
     *
     * <p>Driven by the declarations rather than by the recorded runs, so a graph
     * that has never been reached is listed as such instead of vanishing. That
     * is the state the whole panel exists to make visible: a graph silently
     * absent looks like one that is fine.
     */
    private List<GraphProgress> graphProgress() {
        val runs = sourceGraphProgress
            .map(SourceGraphProgress::lastRun)
            .orElseGet(Map::of);
        return sourceGraphProviders.stream()
            .flatMap(provider -> provider.sourceGraphs().stream())
            .map(declared -> progressOf(declared, runs.get(declared.graph())))
            .toList();
    }

    private static GraphProgress progressOf(SourceGraph declared, SourceGraphProgress.Run run) {
        if (run == null) {
            return new GraphProgress(declared.graph(), declared.title(), NOT_RUN,
                false, false, 0, 0, 0);
        }
        return new GraphProgress(declared.graph(), declared.title(), run.state().label(),
            run.state().warning(), true, run.described(), run.entities(), run.outstanding());
    }

    @RequestMapping(value="/documents/optimize",
                    method = RequestMethod.POST)
    @ResponseBody
    public HttpEntity<MaintenanceResponse> optimizeRepository() {
        try {
            repoService.performOptimization();
            return ResponseEntity.ok(loadMaintenancePage().addMessage("Optimized repository"));
        }
        catch(DataRepositoryException ex) {
            MaintenanceResponse response = loadMaintenancePage().addMessage(ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @RequestMapping(value="/documents/reindex",
                    method = RequestMethod.POST)
    @ResponseBody
    public HttpEntity<MaintenanceResponse> reindexDocuments() {
        try {
            solrIndex.rebuildIndex();
            return ResponseEntity.ok(loadMaintenancePage().addMessage("All documents successfully indexed"));
        }
        catch(DocumentIndexingException die) {
            MaintenanceResponse response = loadMaintenancePage().addMessage(die.getMessage());
            Arrays.stream(die.getSuppressed()).forEach(e -> response.addMessage(e.getMessage()));
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @RequestMapping(value="/links/reindex",
                    method = RequestMethod.POST)
    @ResponseBody
    public HttpEntity<MaintenanceResponse> reindexLinks() {
        try {
            linkingService.rebuildIndex();
            return ResponseEntity.ok(loadMaintenancePage().addMessage("All documents successfully linked"));
        } catch (DocumentIndexingException ex) {
            MaintenanceResponse response = loadMaintenancePage().addMessage(ex.getMessage());
            Arrays.stream(ex.getSuppressed()).forEach(e -> response.addMessage(e.getMessage()));
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @RequestMapping(value="/mapfiles/reindex",
                    method = RequestMethod.POST)
    @ResponseBody
    public HttpEntity<MaintenanceResponse> recreateMapFiles() {
        try {
            mapserverService.rebuildIndex();
            return ResponseEntity.ok(loadMaintenancePage().addMessage("All mapfiles successfully created"));
        } catch (DocumentIndexingException ex) {
            MaintenanceResponse response = loadMaintenancePage().addMessage(ex.getMessage());
            Arrays.stream(ex.getSuppressed()).forEach(e -> response.addMessage(e.getMessage()));
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * Triggers the Fuseki export on demand, rather than waiting for its once-a-day {@code @Scheduled}
     * run. Blocks the request thread for the duration of the export - the same trade-off the other
     * maintenance actions above already make for their own potentially slow rebuilds - rather than
     * running it asynchronously, so success/failure can be reported in this one response exactly like
     * the other actions.
     */
    @RequestMapping(value="/exports/fuseki",
                    method = RequestMethod.POST)
    @ResponseBody
    public HttpEntity<MaintenanceResponse> exportToFuseki() {
        if (catalogueExportService.isEmpty()) {
            MaintenanceResponse response = loadMaintenancePage()
                .addMessage("Fuseki export is not available: the 'exports' profile is not active");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(response);
        }
        try {
            catalogueExportService.get().runExport();
            return ResponseEntity.ok(loadMaintenancePage().addMessage("Fuseki export completed"));
        } catch (Exception ex) {
            // Deliberately wide. RestClientResponseException only covers a 4xx/5xx *response*
            // from Fuseki; a refused connection or timeout is ResourceAccessException, a sibling
            // rather than a subclass, and CatalogueToTurtleService.refresh() is @SneakyThrows over
            // IOException/TemplateException - which cannot be named in a catch here at all, since
            // the compiler cannot see them being thrown. Anything narrower escapes as a bare 500
            // instead of being reported on the page, which is what this endpoint exists to avoid.
            MaintenanceResponse response = loadMaintenancePage().addMessage(ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }
}
