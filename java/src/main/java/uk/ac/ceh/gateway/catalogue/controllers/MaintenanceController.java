package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkingException;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
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
    private final DocumentIndexingService solrIndex;
    private final DocumentLinkService linkingService;
    private final TerraCatalogImporterService terraCatalogImporterService;
    
    @Autowired
    public MaintenanceController(DocumentIndexingService solrIndex, DocumentLinkService linkingService, TerraCatalogImporterService terraCatalogImporterService) {
        this.solrIndex = solrIndex;
        this.linkingService = linkingService;
        this.terraCatalogImporterService = terraCatalogImporterService;
    }
    
    @RequestMapping (method = RequestMethod.GET)
    public String loadMaintenancePage() {
        return "/html/maintenance.html.tpl";
    }
    
    @RequestMapping(value="/documents/reindex",
                    method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void reindexDocuments() throws DocumentIndexingException {
        solrIndex.rebuildIndex();
    }
    
    @RequestMapping(value="/links/reindex",
                    method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void reindexLinks() throws DocumentLinkingException {
        linkingService.rebuildLinks();
    }
    
    @RequestMapping(value="/terraCatalogImport",
                    method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String importTerraCatalogZip(
            @ActiveUser CatalogueUser user,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "No file has been uploaded";
        }
        
        try {
            Path zipFile = Files.createTempFile("terraCatalogImport", ".zip");
            try {
                file.transferTo(zipFile.toFile());

                terraCatalogImporterService
                        .getImporter(user)
                        .importFile(new ZipFile(zipFile.toFile()));
                return "File imported okay";
            }
            finally {
                Files.delete(zipFile);
            }
        }
        catch(IOException | UnknownContentTypeException ex) {
            return "failed to upload" + ex.getMessage();
        }
    }
}