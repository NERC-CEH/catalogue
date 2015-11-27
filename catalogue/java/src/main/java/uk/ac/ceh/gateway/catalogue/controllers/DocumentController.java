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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
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
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoFactory;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentTypeLookupService;
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
    private final DocumentWritingService documentWriter;
    private final PostProcessingService postProcessingService;
    private final DocumentTypeLookupService documentTypeLookupService;
    
    @Autowired
    public DocumentController(  DataRepository<CatalogueUser> repo,
                                DocumentIdentifierService documentIdentifierService,
                                DocumentReadingService documentReader,
                                DocumentInfoMapper documentInfoMapper,
                                DocumentInfoFactory<MetadataDocument, MetadataInfo> infoFactory,
                                BundledReaderService<MetadataDocument> documentBundleReader,
                                DocumentWritingService documentWritingService,
                                PostProcessingService postProcessingService,
                                DocumentTypeLookupService documentTypeLookupService) {
        this.repo = repo;
        this.documentIdentifierService = documentIdentifierService;
        this.documentReader = documentReader;
        this.documentInfoMapper = documentInfoMapper;
        this.infoFactory = infoFactory;
        this.documentBundleReader = documentBundleReader;
        this.documentWriter = documentWritingService;
        this.postProcessingService = postProcessingService;
        this.documentTypeLookupService = documentTypeLookupService;
    }
    
    @Secured(EDITOR_ROLE)
    @RequestMapping (value = "documents/upload",
                     method = RequestMethod.GET)
    public ModelAndView uploadForm() {
        return new ModelAndView("/html/upload.html.tpl");
    }
    
    @Secured(EDITOR_ROLE)
    @RequestMapping (value = "documents",
                     method = RequestMethod.POST,
                     consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity uploadDocument(
            @ActiveUser CatalogueUser user,
            @RequestParam(value = "message", defaultValue = "new document") String commitMessage,
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("type") String type) throws IOException, UnknownContentTypeException  {
        
        MediaType contentMediaType = MediaType.parseMediaType(multipartFile.getContentType());
        Path tmpFile = Files.createTempFile("upload", null); //Create a temp file to upload the input stream to
        String fileId;
        MetadataDocument data;
        Class<? extends MetadataDocument> metadataType = documentTypeLookupService.getType(type);
        
        try {
            Files.copy(multipartFile.getInputStream(), tmpFile, StandardCopyOption.REPLACE_EXISTING); //copy the file so that we can pass over multiple times
            
            //the documentReader will close the underlying inputstream
            data = documentReader.read(Files.newInputStream(tmpFile), contentMediaType, metadataType); 
            MetadataInfo metadataInfo = createMetadataInfoWithDefaultPermissions(data, user, contentMediaType); //get the metadata info
            
            fileId = Optional.ofNullable(documentIdentifierService.generateFileId(data.getId()))
                             .orElse(documentIdentifierService.generateFileId());
            
            repo.submitData(String.format("%s.meta", fileId), (o)-> documentInfoMapper.writeInfo(metadataInfo, o) )
                .submitData(String.format("%s.raw", fileId), (o) -> Files.copy(tmpFile, o) )
                .commit(user, commitMessage);
        }
        finally {
            Files.delete(tmpFile); //file no longer needed
        }
        if (data.getId() == null && data instanceof GeminiDocument) {
            GeminiDocument geminiDocument = ((GeminiDocument) data);
            updateIdAndMetadataDate(geminiDocument, fileId);
            
            repo.submitData(String.format("%s.meta", fileId), (o)-> documentInfoMapper.writeInfo(updatingRawType(fileId), o))
                .submitData(String.format("%s.raw", fileId), (o) -> documentWriter.write(geminiDocument, MediaType.APPLICATION_JSON, o))
                .commit(user, String.format("Adding Id to: %s", fileId));
        }
        
        return seeOther(fileId);
    }
    
    private ResponseEntity seeOther(String fileId) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Location", documentIdentifierService.generateUri(fileId));
        return new ResponseEntity(headers, HttpStatus.SEE_OTHER);
    }
    
    @Secured(EDITOR_ROLE)
    @RequestMapping (value = "documents",
                     method = RequestMethod.POST,
                     consumes = GEMINI_JSON_VALUE)
    public ResponseEntity<MetadataDocument> uploadDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody GeminiDocument geminiDocument) throws IOException, UnknownContentTypeException, PostProcessingException  {
       
        String id = documentIdentifierService.generateFileId();
        updateIdAndMetadataDate(geminiDocument, id);
        URI recordUri = URI.create(documentIdentifierService.generateUri(id));
        addRecordUriAsResourceIdentifier(geminiDocument, recordUri);

        MetadataInfo metadataInfo = createMetadataInfoWithDefaultPermissions(geminiDocument, user, MediaType.APPLICATION_JSON);
        
        repo.submitData(String.format("%s.meta", id), (o)-> documentInfoMapper.writeInfo(metadataInfo, o) )
            .submitData(String.format("%s.raw", id), (o) -> documentWriter.write(geminiDocument, MediaType.APPLICATION_JSON, o))
            .commit(user, String.format("new Gemini document: %s", id));
                
        return ResponseEntity
            .created(recordUri)
            .body(readMetadata(user, id));
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
    
    private MetadataInfo createMetadataInfoWithDefaultPermissions(MetadataDocument document, CatalogueUser user, MediaType mediaType) {
        MetadataInfo toReturn = infoFactory.createInfo(document, mediaType);
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
            @RequestBody GeminiDocument geminiDocument) throws IOException, DataRepositoryException, UnknownContentTypeException, DocumentIndexingException, PostProcessingException {
        
        
        MetadataInfo metadataInfo = updatingRawType(file);
        updateIdAndMetadataDate(geminiDocument, file);
        URI recordUri = URI.create(documentIdentifierService.generateUri(file));
        addRecordUriAsResourceIdentifier(geminiDocument, recordUri);
        
        repo.submitData(String.format("%s.meta", file), (o)-> documentInfoMapper.writeInfo(metadataInfo, o))
            .submitData(String.format("%s.raw", file), (o) -> documentWriter.write(geminiDocument, MediaType.APPLICATION_JSON, o))
            .commit(user, String.format("edit Gemini document: %s", file));
        
        return ResponseEntity
            .ok(readMetadata(user, file));
    }
    
    private MetadataInfo updatingRawType(String file) throws IOException, DataRepositoryException, UnknownContentTypeException {
        MetadataInfo metadataInfo = documentBundleReader.readBundle(file, getLatestRevision()).getMetadata();
        metadataInfo.setRawType(GEMINI_JSON_VALUE);
        return metadataInfo;
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.GET)   
    @ResponseBody
    public MetadataDocument readMetadata(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        MetadataDocument document = documentBundleReader.readBundle(file, getLatestRevision());
        document.attachUri(URI.create(documentIdentifierService.generateUri(file)));
        postProcessingService.postProcess(document);
        log.debug("document requested: {}", document);
        return document;
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, #revision, 'VIEW')")
    @RequestMapping(value = "history/{revision}/{file}",
                    method = RequestMethod.GET)
    @ResponseBody
    public MetadataDocument readMetadata(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @PathVariable("revision") String revision) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        MetadataDocument document = documentBundleReader.readBundle(file, revision);
        document.attachUri(URI.create(documentIdentifierService.generateUri(file, revision)));
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
    
    private String getLatestRevision() throws DataRepositoryException {
        DataRevision<CatalogueUser> latestRev = repo.getLatestRevision();
        if (latestRev != null) {
            return latestRev.getRevisionID();
        }
        else {
            throw new ResourceNotFoundException("Could not find the requested file");
        }
    }
}
