package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource.IdentityPermissions;
import uk.ac.ceh.gateway.catalogue.permission.CataloguePermission;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;

import java.util.Optional;

import static uk.ac.ceh.gateway.catalogue.model.MetadataInfo.PUBLIC_GROUP;

@Slf4j
@ToString
@Controller
public class PermissionController {
    private final PermissionService permissionService;
    private final DocumentRepository documentRepository;

    public PermissionController(PermissionService permissionService,
                                DocumentRepository documentRepository)
    {
        this.permissionService = permissionService;
        this.documentRepository = documentRepository;
        log.info("Creating {}", this);
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
        document.setMetadata(removeAddedPublicGroupIfNotPublisher(document.getMetadata(), permissionResource));
        documentRepository.save(user, document, file, String.format("Permissions of %s changed.", file));
        return ResponseEntity.ok(new PermissionResource(document));
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
