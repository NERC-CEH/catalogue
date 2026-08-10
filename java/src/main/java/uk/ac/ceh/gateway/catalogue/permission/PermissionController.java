package uk.ac.ceh.gateway.catalogue.permission;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.controllers.IfMatchRevision;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource.IdentityPermissions;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import java.io.IOException;

import java.util.Optional;

import static uk.ac.ceh.gateway.catalogue.model.MetadataInfo.PUBLIC_GROUP;

@Slf4j
@ToString
@Controller
public class PermissionController {
    private final PermissionService permissionService;
    private final DocumentRepository documentRepository;
    private final CachedDataRepository cachedDataRepository;

    public PermissionController(PermissionService permissionService,
                                DocumentRepository documentRepository,
                                CachedDataRepository cachedDataRepository)
    {
        this.permissionService = permissionService;
        this.documentRepository = documentRepository;
        this.cachedDataRepository = cachedDataRepository;
        log.info("Creating");
    }

    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(method = RequestMethod.GET, value = "documents/{file}/permission")
    @ResponseBody
    public HttpEntity<PermissionResource> currentPermission (
            @ActiveUser CatalogueUser user,
            @PathVariable String file
    ) throws DocumentRepositoryException, IOException {
        PermissionResource resource = new PermissionResource(documentRepository.read(file));
        String revision = cachedDataRepository.getDocumentRevisionToken(file);
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        if (revision != null) {
            builder.eTag(revision); // Spring quotes this into a strong ETag: "revision"
        }
        return builder.body(resource);
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
            @PathVariable String file,
            @RequestBody PermissionResource permissionResource,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch)
        throws DocumentRepositoryException, IOException {
        String expectedRevision = IfMatchRevision.require(ifMatch);
        MetadataDocument document = documentRepository.read(file);
        document.setMetadata(removeAddedPublicGroupIfNotPublisher(document.getMetadata(), permissionResource));
        documentRepository.save(user, document, file, String.format("Permissions of %s changed.", file), expectedRevision);
        String newRevision = cachedDataRepository.getDocumentRevisionToken(file);
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        if (newRevision != null) {
            builder.eTag(newRevision); // Spring quotes this into a strong ETag: "revision"
        }
        return builder.body(new PermissionResource(document));
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
