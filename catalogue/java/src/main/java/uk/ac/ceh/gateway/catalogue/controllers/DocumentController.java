package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.GEMINI_JSON_VALUE;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoFactory;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
@Controller
@Slf4j
public class DocumentController {
    public static final String EDITOR_ROLE = "ROLE_CIG_EDITOR";
    public static final String PUBLISHER_ROLE = "ROLE_CIG_PUBLISHER";
    public static final String MAINTENANCE_ROLE = "ROLE_CIG_SYSTEM_ADMIN";
    private final DataRepository<CatalogueUser> repo;
    private final DocumentIdentifierService documentIdentifierService;
    private final DocumentReadingService documentReader;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final DocumentInfoFactory<MetadataDocument, MetadataInfo> infoFactory;
    private final BundledReaderService<MetadataDocument> documentBundleReader;
    private final DocumentWritingService<MetadataDocument> documentWriter;
    private final PostProcessingService postProcessingService;
    
    @Autowired
    public DocumentController(  DataRepository<CatalogueUser> repo,
                                DocumentIdentifierService documentIdentifierService,
                                DocumentReadingService documentReader,
                                DocumentInfoMapper documentInfoMapper,
                                DocumentInfoFactory<MetadataDocument, MetadataInfo> infoFactory,
                                BundledReaderService<MetadataDocument> documentBundleReader,
                                DocumentWritingService<MetadataDocument> documentWritingService,
                                PostProcessingService postProcessingService) {
        this.repo = repo;
        this.documentIdentifierService = documentIdentifierService;
        this.documentReader = documentReader;
        this.documentInfoMapper = documentInfoMapper;
        this.infoFactory = infoFactory;
        this.documentBundleReader = documentBundleReader;
        this.documentWriter = documentWritingService;
        this.postProcessingService = postProcessingService;
    }
    
    @Secured(EDITOR_ROLE)
    @RequestMapping (value = "documents",
                     method = RequestMethod.POST)
    public ResponseEntity uploadDocument(
            @ActiveUser CatalogueUser user,
            @RequestParam(value = "message", defaultValue = "new document") String commitMessage,
            HttpServletRequest request,
            @RequestHeader("Content-Type") String contentType) throws IOException, UnknownContentTypeException  {
        
        MediaType contentMediaType = MediaType.parseMediaType(contentType);
        Path tmpFile = Files.createTempFile("upload", null); //Create a temp file to upload the input stream to
        String fileId;
        GeminiDocument data;
        try {
            Files.copy(request.getInputStream(), tmpFile, StandardCopyOption.REPLACE_EXISTING); //copy the file so that we can pass over multiple times
            
            //the documentReader will close the underlying inputstream
            data = documentReader.read(Files.newInputStream(tmpFile), contentMediaType, GeminiDocument.class); 
            MetadataInfo metadataDocument = infoFactory.createInfo(data, contentMediaType); //get the metadata info
            
            fileId = Optional.ofNullable(documentIdentifierService.generateFileId(data.getId()))
                             .orElse(documentIdentifierService.generateFileId());
            
            repo.submitData(String.format("%s.meta", fileId), (o)-> documentInfoMapper.writeInfo(metadataDocument, o) )
                .submitData(String.format("%s.raw", fileId), (o) -> Files.copy(tmpFile, o) )
                .commit(user, commitMessage);
        }
        finally {
            Files.delete(tmpFile); //file no longer needed
        }
        return ResponseEntity
            .created(getCurrentUri(request, fileId, repo.getLatestRevision().getRevisionID()))
            .build();
    }
    
    @Secured(EDITOR_ROLE)
    @RequestMapping (value = "documents",
                     method = RequestMethod.POST,
                     consumes = GEMINI_JSON_VALUE)
    public ResponseEntity<MetadataDocument> uploadDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody GeminiDocument geminiDocument,
            HttpServletRequest request) throws IOException, UnknownContentTypeException, PostProcessingException  {
       
        String id = documentIdentifierService.generateFileId();
        updateIdAndMetadataDate(geminiDocument, id);
        URI recordUri = getCurrentUri(request, id, repo.getLatestRevision().getRevisionID());
        addRecordUriAsResourceIdentifier(geminiDocument, recordUri);

        MetadataInfo metadataInfo = createMetadataInfoWithDefaultPermissions(geminiDocument, user);
        
        repo.submitData(String.format("%s.meta", id), (o)-> documentInfoMapper.writeInfo(metadataInfo, o) )
            .submitData(String.format("%s.raw", id), (o) -> documentWriter.write(geminiDocument, o))
            .commit(user, String.format("new Gemini document: %s", id));
                
        return ResponseEntity
            .created(recordUri)
            .body(readMetadata(user, id, request));
    }
    
    private void updateIdAndMetadataDate(GeminiDocument document, String id) {
        document.setId(id).setMetadataDate(LocalDateTime.now());
    }
    
    protected void addRecordUriAsResourceIdentifier(GeminiDocument document, URI recordUri) {
        List<ResourceIdentifier> resourceIdentifiers;
        
        if (document.getResourceIdentifiers() != null) {
            resourceIdentifiers = new ArrayList(document.getResourceIdentifiers());
        } else {
            resourceIdentifiers = new ArrayList<>();
        }

        ResourceIdentifier self = ResourceIdentifier.builder()
            .code(recordUri.toString())
            .build();

        if (!resourceIdentifiers.contains(self)) {
            resourceIdentifiers.add(self);
        }
        document.setResourceIdentifiers(resourceIdentifiers);
    }
    
    private MetadataInfo createMetadataInfoWithDefaultPermissions(MetadataDocument document, CatalogueUser user) {
        MetadataInfo toReturn = infoFactory.createInfo(document, MediaType.parseMediaType(GEMINI_JSON_VALUE));
        String username = user.getUsername();
        toReturn.addPermission(Permission.VIEW, username);
        toReturn.addPermission(Permission.EDIT, username);
        toReturn.addPermission(Permission.DELETE, username);
        return toReturn;
    }
    
    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.PUT,
                    consumes = GEMINI_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody GeminiDocument geminiDocument,
            HttpServletRequest request) throws IOException, DataRepositoryException, UnknownContentTypeException, DocumentIndexingException, PostProcessingException {
        
        
        MetadataInfo metadataInfo = updatingRawType(file);
        updateIdAndMetadataDate(geminiDocument, file);
        URI recordUri = getCurrentUri(request, file, repo.getLatestRevision().getRevisionID());
        addRecordUriAsResourceIdentifier(geminiDocument, recordUri);
        
        repo.submitData(String.format("%s.meta", file), (o)-> documentInfoMapper.writeInfo(metadataInfo, o))
            .submitData(String.format("%s.raw", file), (o) -> documentWriter.write(geminiDocument, o))
            .commit(user, String.format("edit Gemini document: %s", file));
        
        return ResponseEntity
            .ok(readMetadata(user, file, request));
    }
    
    private MetadataInfo updatingRawType(String file) throws IOException, DataRepositoryException, UnknownContentTypeException {
        MetadataInfo metadataInfo = documentBundleReader.readBundle(file, repo.getLatestRevision().getRevisionID()).getMetadata();
        metadataInfo.setRawType(GEMINI_JSON_VALUE);
        return metadataInfo;
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.GET)   
    @ResponseBody
    public MetadataDocument readMetadata(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file, HttpServletRequest request) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        DataRevision<CatalogueUser> latestRev = repo.getLatestRevision();
        return readMetadata(user, file, latestRev.getRevisionID(), request);
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, #revision, 'VIEW')")
    @RequestMapping(value = "history/{revision}/{file}",
                    method = RequestMethod.GET)
    @ResponseBody
    public MetadataDocument readMetadata(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @PathVariable("revision") String revision,
            HttpServletRequest request) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        MetadataDocument document = documentBundleReader.readBundle(file, revision);
        document.attachUri(getCurrentUri(request, file, revision));
        postProcessingService.postProcess(document);
        log.debug("document requested: {}", document);
        return document;
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, 'DELETE')")
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.DELETE)
    @ResponseBody
    public DataRevision<CatalogueUser> deleteDocument(
            @ActiveUser CatalogueUser user,
            @RequestParam(value = "message", defaultValue = "delete document") String reason,
            @PathVariable("file") String file) throws DataRepositoryException, IOException {
        return repo.deleteData(file + ".meta")
                   .deleteData(file + ".raw")
                   .commit(user, reason);
    }
    
    protected URI getCurrentUri(HttpServletRequest request, String file, String revision) throws DataRepositoryException {
        return ServletUriComponentsBuilder
             .fromHttpUrl(getLinkUrlFragment(request, revision))
             .path(file)
             .build()
             .toUri();
    }
    
    private String getLinkUrlFragment(HttpServletRequest request, String revision) throws DataRepositoryException {
        if(revision.equals(repo.getLatestRevision().getRevisionID())) {
           return ServletUriComponentsBuilder
                .fromContextPath(request)
                .path("/id/")
                .build()
                .toUriString();
        }
        else {
            return ServletUriComponentsBuilder
                .fromContextPath(request)
                .path("/history/")
                .path(revision)
                .path("/")
                .build()
                .toUriString();
        }
    }
}
