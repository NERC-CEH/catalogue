package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DocumentDoesNotExistException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

@Controller
@RequestMapping(value = "documents/{file}/permission")
public class PermissionController {
    private final DataRepository<CatalogueUser> repo;
    private final BundledReaderService<MetadataDocument> documentBundleReader;

    @Autowired
    public PermissionController(DataRepository<CatalogueUser> repo, BundledReaderService<MetadataDocument> documentBundleReader) {
        this.repo = repo;
        this.documentBundleReader = documentBundleReader;
    }
    
    @Secured({DocumentController.EDITOR_ROLE, DocumentController.PUBLISHER_ROLE})
    @RequestMapping(method =  RequestMethod.GET)
    @ResponseBody
    public HttpEntity<PermissionResource> currentPermission (
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file) {
        MetadataDocument document = getMetadataDocument(file);
        return ResponseEntity.ok(new PermissionResource(document)); 
    }
    
    private MetadataDocument getMetadataDocument(String fileIdentifier) {
        MetadataDocument toReturn;
        try {
            toReturn = documentBundleReader.readBundle(fileIdentifier, repo.getLatestRevision().getRevisionID());
            if (toReturn.getUri() == null) {
                toReturn.attachUri(URI.create(String.format("/documents/%s", fileIdentifier)));
            }
        } catch (IOException | UnknownContentTypeException ex) {
            throw new DocumentDoesNotExistException(String.format("Document: %s does not exist", fileIdentifier), ex);
        }
        return toReturn;
    }

}