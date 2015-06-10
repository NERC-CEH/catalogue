package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkingException;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MaintenanceResponse;
import uk.ac.ceh.gateway.catalogue.services.TerraCatalogImporterService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author Christopher Johnson
 */
@Controller
@RequestMapping("maintenance")
@Secured(DocumentController.MAINTENANCE_ROLE)
public class MaintenanceController {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentIndexingService solrIndex;
    private final DocumentLinkService linkingService;
    private final TerraCatalogImporterService terraCatalogImporterService;
    
    @Autowired
    public MaintenanceController(DataRepository<CatalogueUser> repo, DocumentIndexingService solrIndex, DocumentLinkService linkingService, TerraCatalogImporterService terraCatalogImporterService) {
        this.repo = repo;
        this.solrIndex = solrIndex;
        this.linkingService = linkingService;
        this.terraCatalogImporterService = terraCatalogImporterService;
    }
    
    @RequestMapping (method = RequestMethod.GET)
    @ResponseBody
    public MaintenanceResponse loadMaintenancePage() {
        MaintenanceResponse toReturn = new MaintenanceResponse();
        toReturn.setLinked(!linkingService.isEmpty());
        try {
            toReturn.setIndexed(!solrIndex.isIndexEmpty());
        } catch(DocumentIndexingException ex) {
            toReturn.addMessage(ex.getMessage());
        }
        try {
            toReturn.setLatestRevision(repo.getLatestRevision());
        } catch(DataRepositoryException dre) {
            toReturn.addMessage(dre.getMessage());
        }
        return toReturn;
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
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(loadMaintenancePage().addMessage(die.getMessage()));
        }
    }
    
    @RequestMapping(value="/links/reindex",
                    method = RequestMethod.POST)
    @ResponseBody
    public HttpEntity<MaintenanceResponse> reindexLinks() {
        try {
            linkingService.rebuildLinks();
            return ResponseEntity.ok(loadMaintenancePage().addMessage("All documents successfully linked"));
        } catch (DocumentLinkingException ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(loadMaintenancePage().addMessage(ex.getMessage()));
        }
    }
    
    @RequestMapping(value="/terraCatalogImport",
                    method = RequestMethod.POST)
    @ResponseBody
    public HttpEntity<MaintenanceResponse> importTerraCatalogZip(
            @ActiveUser CatalogueUser user,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(loadMaintenancePage().addMessage("No file has been uploaded"));
        }
        
        try {
            Path zipFile = Files.createTempFile("terraCatalogImport", ".zip");
            try {
                file.transferTo(zipFile.toFile());
                try (ZipFile zip = new ZipFile(zipFile.toFile())) {
                    terraCatalogImporterService
                            .getImporter(user)
                            .importFile(zip);
                    return ResponseEntity.ok(loadMaintenancePage().addMessage("Zip has been imported succcessfully"));
                }
            }
            finally {
                Files.delete(zipFile);
            }
        }
        catch(IOException | UnknownContentTypeException ex) {
            return ResponseEntity
                    .badRequest()
                    .body(loadMaintenancePage().addMessage(ex.getMessage()));
        }
    }
}