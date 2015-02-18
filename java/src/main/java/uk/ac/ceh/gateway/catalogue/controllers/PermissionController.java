package uk.ac.ceh.gateway.catalogue.controllers;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource;
import uk.ac.ceh.gateway.catalogue.services.MetadataInfoEditingService;

@Controller
@RequestMapping(value = "documents/{file}/permission")
@Slf4j
public class PermissionController {
    private final MetadataInfoEditingService metadataInfoEditingService;

    @Autowired
    public PermissionController(MetadataInfoEditingService metadataInfoEditingService) {
        this.metadataInfoEditingService = metadataInfoEditingService;
    }
    
    @Secured({DocumentController.EDITOR_ROLE, DocumentController.PUBLISHER_ROLE})
    @RequestMapping(method =  RequestMethod.GET)
    @ResponseBody
    public HttpEntity<PermissionResource> currentPermission (
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file) {
        URI metadataUri = getMetadataUri(file);
        MetadataDocument document = metadataInfoEditingService.getMetadataDocument(file, metadataUri);
        return ResponseEntity.ok(new PermissionResource(document)); 
    }
    
    @Secured({DocumentController.EDITOR_ROLE, DocumentController.PUBLISHER_ROLE})
    @RequestMapping(method =  RequestMethod.PUT)
    @ResponseBody
    public HttpEntity<PermissionResource> updatePermission (
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody PermissionResource permissionResource) throws DataRepositoryException {
        log.debug("Updating permissions to: {} for: {}", permissionResource, file);
        URI metadataUri = getMetadataUri(file);
        MetadataInfo original = metadataInfoEditingService.getMetadataDocument(file, metadataUri).getMetadata();
        MetadataInfo returned = permissionResource.updatePermissions(original);
        if ( !returned.equals(original)) {
            String commitMsg = String.format("Permissions of %s changed.", file);
            metadataInfoEditingService.saveMetadataInfo(file, returned, user, commitMsg);
        }
        log.debug("Updated permissions now: {}", returned);
        return ResponseEntity.ok(new PermissionResource(metadataInfoEditingService.getMetadataDocument(file, metadataUri))); 
    }
    
    private URI getMetadataUri(String file) {
        return MvcUriComponentsBuilder
            .fromMethodName(DocumentController.class, "readMetadata", null, file, null)
            .buildAndExpand(file)
            .toUri();
    }
}