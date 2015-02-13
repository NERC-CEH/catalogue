package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
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
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DocumentDoesNotExistException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

@Controller
@RequestMapping(value = "documents/{file}/permission")
@Slf4j
public class PermissionController {
    private final DataRepository<CatalogueUser> repo;
    private final BundledReaderService<MetadataDocument> documentBundleReader;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;

    @Autowired
    public PermissionController(DataRepository<CatalogueUser> repo, BundledReaderService<MetadataDocument> documentBundleReader, DocumentInfoMapper<MetadataInfo> documentInfoMapper) {
        this.repo = repo;
        this.documentBundleReader = documentBundleReader;
        this.documentInfoMapper = documentInfoMapper;
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
    
    @Secured({DocumentController.EDITOR_ROLE, DocumentController.PUBLISHER_ROLE})
    @RequestMapping(method =  RequestMethod.PUT)
    @ResponseBody
    public HttpEntity<PermissionResource> updatePermission (
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody PermissionResource permissionResource) throws DataRepositoryException {
        log.debug("Updating permissions to: {} for: {}, binding result: {}", permissionResource, file);
      
        MetadataInfo updated = permissionResource.updatePermissions(getMetadataInfo(file));
        saveMetadataInfo(file, updated, user);
        log.debug("Updated permissions now: {}", updated);
        return ResponseEntity.ok(new PermissionResource(getMetadataDocument(file))); 
    }
    
    private MetadataDocument getMetadataDocument(String fileIdentifier) {
        MetadataDocument toReturn;
        try {
            toReturn = documentBundleReader.readBundle(fileIdentifier, repo.getLatestRevision().getRevisionID());
            if (toReturn.getUri() == null) {
                URI metadataUri = MvcUriComponentsBuilder
                    .fromMethodName(DocumentController.class, "readMetadata", null, fileIdentifier, null)
                    .buildAndExpand(fileIdentifier)
                    .toUri();
                toReturn.attachUri(metadataUri);
            }
        } catch (IOException | UnknownContentTypeException ex) {
            throw new DocumentDoesNotExistException(String.format("Document: %s does not exist", fileIdentifier), ex);
        }
        return toReturn;
    }
    
    private MetadataInfo getMetadataInfo(String fileIdentifier) {
        MetadataInfo toReturn;
        try {
            toReturn = documentInfoMapper.readInfo(repo.getData(String.format("%s.meta", fileIdentifier)).getInputStream());
        } catch (IOException | NullPointerException ex) {
            throw new DocumentDoesNotExistException(String.format("Document: %s does not exist", fileIdentifier), ex);
        }
        return toReturn;
    }
    
    private void saveMetadataInfo(String fileIdentifier, MetadataInfo info, CatalogueUser user) throws DataRepositoryException {
        try {
            String commitMsg = String.format("Permissions of %s changed.", fileIdentifier);
            repo.submitData(String.format("%s.meta", fileIdentifier), 
                (o)-> documentInfoMapper
                    .writeInfo(info, o))
                    .commit(user, commitMsg);
        } catch (DataRepositoryException ex) {
            throw new DataRepositoryException(String.format("Could not save permissions for: %s", fileIdentifier), ex);
        }
    }
}