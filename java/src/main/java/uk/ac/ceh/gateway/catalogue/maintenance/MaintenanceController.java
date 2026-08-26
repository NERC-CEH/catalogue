package uk.ac.ceh.gateway.catalogue.maintenance;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientResponseException;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.controllers.DocumentController;
import uk.ac.ceh.gateway.catalogue.exports.CatalogueExportService;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.mapserver.MapServerIndexingService;

import java.util.Arrays;
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

    /**
     * {@code CatalogueExportService} only exists under the {@code exports} profile, but this controller
     * is not profile-gated - every catalogue context (with or without {@code exports}) builds a
     * {@code MaintenanceController}. A plain constructor dependency on {@code CatalogueExportService}
     * would therefore stop any non-{@code exports} context from starting. {@code Optional} lets Spring
     * inject an empty value when the bean is absent instead of failing to wire the controller at all.
     */
    public MaintenanceController(
        DataRepositoryOptimizingService repoService,
        @Qualifier("solr-index") DocumentIndexingService solrIndex,
        @Qualifier("jena-index") DocumentIndexingService linkingService,
        @Qualifier("mapserver-index") DocumentIndexingService mapserverService,
        Optional<CatalogueExportService> catalogueExportService
    ) {
        this.repoService = repoService;
        this.solrIndex = solrIndex;
        this.linkingService = linkingService;
        this.mapserverService = (MapServerIndexingService) mapserverService;
        this.catalogueExportService = catalogueExportService;
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
        return toReturn;
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
        } catch (RestClientResponseException ex) {
            MaintenanceResponse response = loadMaintenancePage().addMessage(ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }
}
