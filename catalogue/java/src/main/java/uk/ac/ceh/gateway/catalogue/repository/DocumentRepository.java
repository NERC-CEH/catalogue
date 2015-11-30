package uk.ac.ceh.gateway.catalogue.repository;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.datastore.DataWriter;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.GEMINI_JSON_VALUE;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
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

public class DocumentRepository {
    private final DocumentTypeLookupService documentTypeLookupService;
    private final DocumentReadingService documentReader;
    private final DocumentIdentifierService documentIdentifierService;
    private final DocumentInfoFactory<MetadataDocument, MetadataInfo> infoFactory;
    private final DataRepository<CatalogueUser> repo;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final DocumentWritingService documentWriter;
    private final BundledReaderService<MetadataDocument> documentBundleReader;
    private final PostProcessingService postProcessingService;

    @Autowired
    public DocumentRepository(DocumentTypeLookupService documentTypeLookupService,
            DocumentReadingService documentReader,
            DocumentIdentifierService documentIdentifierService,
            DocumentInfoFactory<MetadataDocument, MetadataInfo> infoFactory,
            DataRepository<CatalogueUser> repo,
            DocumentInfoMapper<MetadataInfo> documentInfoMapper,
            DocumentWritingService documentWriter,
            BundledReaderService<MetadataDocument> documentBundleReader,
            PostProcessingService postProcessingService) {
        this.documentTypeLookupService = documentTypeLookupService;
        this.documentReader = documentReader;
        this.documentIdentifierService = documentIdentifierService;
        this.infoFactory = infoFactory;
        this.repo = repo;
        this.documentInfoMapper = documentInfoMapper;
        this.documentWriter = documentWriter;
        this.documentBundleReader = documentBundleReader;
        this.postProcessingService = postProcessingService;
    }
    
    public MetadataDocument read(String file) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        return read(file, getLatestRevision());
    }
    
    public MetadataDocument read(String file, String revision) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        MetadataDocument document = documentBundleReader.readBundle(file, revision);
        postProcessingService.postProcess(document);
        return document;
    }
    
    public MetadataDocument save(CatalogueUser user, MultipartFile multipartFile, String type) throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException {
        MediaType contentMediaType = MediaType.parseMediaType(multipartFile.getContentType());
        Path tmpFile = Files.createTempFile("upload", null); //Create a temp file to upload the input stream to
        String id;
        MetadataDocument data;
        Class<? extends MetadataDocument> metadataType = documentTypeLookupService.getType(type);
        
        try {
            Files.copy(multipartFile.getInputStream(), tmpFile, StandardCopyOption.REPLACE_EXISTING); //copy the file so that we can pass over multiple times
            
            //the documentReader will close the underlying inputstream
            data = documentReader.read(Files.newInputStream(tmpFile), contentMediaType, metadataType); 
            MetadataInfo metadataInfo = createMetadataInfoWithDefaultPermissions(data, user, contentMediaType); //get the metadata info
            
            id = Optional.ofNullable(documentIdentifierService.generateFileId(data.getId()))
                             .orElse(documentIdentifierService.generateFileId());
            
            commit(user, id, "new Gemini XML document: %s", metadataInfo,
            (o) -> Files.copy(tmpFile, o));
        }
        finally {
            Files.delete(tmpFile); //file no longer needed
        }
        
        if (data instanceof GeminiDocument) {
            data = save(user, (GeminiDocument)data, id);
        }
        
        return data;
    }
    
    public MetadataDocument save(CatalogueUser user, GeminiDocument geminiDocument) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {       
        return save(user, 
            geminiDocument,
            createMetadataInfoWithDefaultPermissions(geminiDocument, user, MediaType.APPLICATION_JSON), 
            documentIdentifierService.generateFileId(),
            "new Gemini document: %s"
        );
    }
    
    public MetadataDocument save(CatalogueUser user, GeminiDocument geminiDocument, String id) throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException {
        return save(user,
            geminiDocument, 
            updatingRawType(id), 
            id, 
            "edit Gemini document: %s"
        );
    }
    
    private MetadataDocument save(CatalogueUser user, GeminiDocument geminiDocument, MetadataInfo metadataInfo, String id, String messageTemplate) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        updateIdAndMetadataDate(geminiDocument, id);
        String uri = documentIdentifierService.generateUri(id);
        addRecordUriAsResourceIdentifier(geminiDocument, uri);
        geminiDocument.attachUri(URI.create(uri));
        
        commit(user, id, messageTemplate, metadataInfo,
            (o) -> documentWriter.write(geminiDocument, MediaType.APPLICATION_JSON, o));
        
        return read(id);
    }
    
    public DataRevision<CatalogueUser> delete(CatalogueUser user, String id) throws DataRepositoryException {
        return repo.deleteData(id + ".meta")
                   .deleteData(id + ".raw")
                   .commit(user, String.format("delete document: %s", id));
    }
    
    private void commit(CatalogueUser user, String id, String messageTemplate, MetadataInfo metadataInfo, DataWriter dataWriter) throws DataRepositoryException {
        repo.submitData(String.format("%s.meta", id), (o)-> documentInfoMapper.writeInfo(metadataInfo, o))
            .submitData(String.format("%s.raw", id), dataWriter)
            .commit(user, String.format(messageTemplate, id));
    }
    
    private MetadataInfo updatingRawType(String id) throws IOException, DataRepositoryException, UnknownContentTypeException {
        MetadataInfo metadataInfo = documentBundleReader.readBundle(id, getLatestRevision()).getMetadata();
        metadataInfo.setRawType(GEMINI_JSON_VALUE);
        return metadataInfo;
    }
    
    private MetadataInfo createMetadataInfoWithDefaultPermissions(MetadataDocument document, CatalogueUser user, MediaType mediaType) {
        MetadataInfo toReturn = infoFactory.createInfo(document, mediaType);
        String username = user.getUsername();
        toReturn.addPermission(Permission.VIEW, username);
        toReturn.addPermission(Permission.EDIT, username);
        toReturn.addPermission(Permission.DELETE, username);
        return toReturn;
    }
    
    private void updateIdAndMetadataDate(GeminiDocument document, String id) {
        document.setId(id).setMetadataDate(LocalDateTime.now());
    }
    
    private void addRecordUriAsResourceIdentifier(GeminiDocument document, String recordUri) {
        List<ResourceIdentifier> resourceIdentifiers;
        
        if (document.getResourceIdentifiers() != null) {
            resourceIdentifiers = new ArrayList(document.getResourceIdentifiers());
        } else {
            resourceIdentifiers = new ArrayList<>();
        }

        ResourceIdentifier self = ResourceIdentifier.builder()
            .code(recordUri)
            .build();

        if (!resourceIdentifiers.contains(self)) {
            resourceIdentifiers.add(self);
        }
        document.setResourceIdentifiers(resourceIdentifiers);
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