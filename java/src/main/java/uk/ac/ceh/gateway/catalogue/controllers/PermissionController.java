package uk.ac.ceh.gateway.catalogue.controllers;

import static uk.ac.ceh.gateway.catalogue.model.MetadataInfo.PUBLIC_GROUP;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.SneakyThrows;
import lombok.val;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CataloguePermission;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource.IdentityPermissions;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.services.PermissionService;

@Controller
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
    @RequestMapping(method = RequestMethod.GET, value = "documents/{file}/permission")
    @ResponseBody
    public HttpEntity<PermissionResource> currentPermission (
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file
    ) throws DocumentRepositoryException {
        return ResponseEntity.ok(
            new PermissionResource(
                documentRepository.read(file)
            )
        ); 
    }

    @RequestMapping(method = RequestMethod.GET, value = "permissions")
    @ResponseBody
    public HttpEntity<CataloguePermission> permissions (
            @ActiveUser CatalogueUser user,
            @RequestParam(value = "catalogue", required = false) String catalogue,
            @RequestParam(value = "id", required = false) String id
    ) throws DocumentRepositoryException {
        val builder = CataloguePermission.builder()
            .identity(user.getUsername())
            .datacite(permissionService.userCanDatacite())
            .groups(permissionService.getGroupsForUser(user));

        if (user.isPublic()) builder.identity("public");

        if (id != null) {
            builder
                .id(id)
                .view(permissionService.toAccess(user, id, "VIEW"))
                .edit(permissionService.userCanEdit(id))
                .upload(permissionService.userCanUpload(id))
                .delete(permissionService.userCanDelete(id));
        }

        if (catalogue == null && id != null) {
            val document = documentRepository.read(id);
            catalogue = document.getCatalogue();
        }

        if (catalogue != null) {
            builder
                .catalogue(catalogue)
                .create(permissionService.userCanCreate(catalogue))
                .makePublic(permissionService.userCanMakePublic(catalogue))
                .editRestrictedFields(permissionService.userCanEditRestrictedFields(catalogue));
        } else {
            builder.create(false);
        }

        return ResponseEntity.ok(builder.build()); 
    }
    
    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(method =  RequestMethod.PUT, value = "documents/{file}/permission")
    @ResponseBody
    public HttpEntity<PermissionResource> updatePermission (
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody PermissionResource permissionResource) 
        throws DocumentRepositoryException {
        MetadataDocument document = documentRepository.read(file);
        if (document instanceof GeminiDocument) {
            GeminiDocument geminiDocument = (GeminiDocument) document;
            String uploadId = geminiDocument.getUploadId();
            if (uploadId != null) copyPermission(user, documentRepository.read(uploadId), permissionResource);
        }
        document.setMetadata(removeAddedPublicGroupIfNotPublisher(document.getMetadata(), permissionResource));
        documentRepository.save(user, document, file, String.format("Permissions of %s changed.", file));
        return ResponseEntity.ok(new PermissionResource(document)); 
    }
    
    @SneakyThrows
    private void copyPermission (CatalogueUser user, MetadataDocument document, PermissionResource permissionResource) {
        MetadataInfo metadataInfo = document.getMetadata();
        metadataInfo = metadataInfo.replaceAllPermissions(permissionResource.getPermissions());
        document.setMetadata(metadataInfo);
        documentRepository.save(user, document, String.format("Permissions of %s changed.", document.getId()));
    }

    private MetadataInfo removeAddedPublicGroupIfNotPublisher(MetadataInfo original, PermissionResource permissionResource) {
        MetadataInfo toReturn; 
        
        if (
            permissionService.userCanMakePublic(original.getCatalogue())
            || original.isPubliclyViewable(Permission.VIEW)
        ){
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