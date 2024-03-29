package uk.ac.ceh.gateway.catalogue.controllers;

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
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.mapserver.MapServerIndexingService;
import uk.ac.ceh.gateway.catalogue.model.MaintenanceResponse;
import uk.ac.ceh.gateway.catalogue.services.DataRepositoryOptimizingService;

import java.util.Arrays;

@Slf4j
@ToString
@Controller
@RequestMapping("maintenance")
@Secured(DocumentController.MAINTENANCE_ROLE)
public class MaintenanceController {
    private final DataRepositoryOptimizingService repoService;
    private final DocumentIndexingService solrIndex;
    private final DocumentIndexingService linkingService;
    private final DocumentIndexingService validationService;
    private final MapServerIndexingService mapserverService;

    public MaintenanceController(
        DataRepositoryOptimizingService repoService,
        @Qualifier("solr-index") DocumentIndexingService solrIndex,
        @Qualifier("jena-index") DocumentIndexingService linkingService,
        @Qualifier("validation-index") DocumentIndexingService validationService,
        @Qualifier("mapserver-index") DocumentIndexingService mapserverService
    ) {
        this.repoService = repoService;
        this.solrIndex = solrIndex;
        this.linkingService = linkingService;
        this.validationService = validationService;
        this.mapserverService = (MapServerIndexingService) mapserverService;
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
            toReturn.setValidated(!validationService.isIndexEmpty());
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

    @RequestMapping(value="/documents/validate",
                    method = RequestMethod.POST)
    @ResponseBody
    public HttpEntity<MaintenanceResponse> validateRepository() {
        try {
            validationService.rebuildIndex();
            return ResponseEntity.ok(loadMaintenancePage().addMessage("Validating repository"));
        }
        catch(DocumentIndexingException ex) {
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
}
