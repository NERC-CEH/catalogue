package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import static uk.ac.ceh.gateway.catalogue.model.MetadataInfo.PUBLIC_GROUP;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource.IdentityPermissions;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.services.PermissionService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

@Controller
@RequestMapping(value = "documents/{file}/permission")
public class PermissionController {
    private final PermissionService permissionService;
    private final DocumentRepository documentRepository;

    @Autowired
    public PermissionController(PermissionService permissionService,
                                DocumentRepository documentRepository)
    {
        this.permissionService = permissionService;
        this.documentRepository = documentRepository;
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<PermissionResource> currentPermission (
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file) 
        throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException
    {
        return ResponseEntity.ok(
            new PermissionResource(
                documentRepository.read(file)
            )
        ); 
    }
    
    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(method =  RequestMethod.PUT)
    @ResponseBody
    public HttpEntity<PermissionResource> updatePermission (
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody PermissionResource permissionResource) 
        throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException
    {
        MetadataDocument document = documentRepository.read(file);
        document.attachMetadata(removeAddedPublicGroupIfNotPublisher(document.getMetadata(), permissionResource));
        documentRepository.save(user, document, file, String.format("Permissions of %s changed.", file));
        return ResponseEntity.ok(new PermissionResource(document)); 
    }
    
    private MetadataInfo removeAddedPublicGroupIfNotPublisher(MetadataInfo original, PermissionResource permissionResource) {
        MetadataInfo toReturn; 
        
        if (permissionService.userCanMakePublic() || original.isPubliclyViewable(Permission.VIEW)) {
            toReturn = permissionResource.updatePermissions(original);
        } else {
            Optional<IdentityPermissions> publicGroup = publicGroup(permissionResource);
            if (publicGroup.isPresent()) {
                permissionResource.getPermissions().remove(publicGroup.get());
                toReturn = permissionResource.updatePermissions(original); 
            } else {
                toReturn = permissionResource.updatePermissions(original);
            }
        }
        return toReturn;
    }
    
    private Optional<IdentityPermissions> publicGroup(PermissionResource permissionResource) {
        return permissionResource.getPermissions()
            .stream()
            .filter((identity) -> identity.getIdentity().equalsIgnoreCase(PUBLIC_GROUP))
            .findFirst();
    }
}