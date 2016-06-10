package uk.ac.ceh.gateway.catalogue.repository;

import java.io.IOException;
import java.io.InputStream;
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
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.GEMINI_JSON_VALUE;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoFactory;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

public class DocumentRepository {
    private final DocumentTypeLookupService documentTypeLookupService;
    private final DocumentReadingService documentReader;
    private final DocumentIdentifierService documentIdentifierService;
    private final DocumentInfoFactory<MetadataDocument, MetadataInfo> infoFactory;
    private final DocumentWritingService documentWriter;
    private final BundledReaderService<MetadataDocument> documentBundleReader;
    private final GitRepoWrapper repo;

    @Autowired
    public DocumentRepository(DocumentTypeLookupService documentTypeLookupService,
            DocumentReadingService documentReader,
            DocumentIdentifierService documentIdentifierService,
            DocumentInfoFactory<MetadataDocument, MetadataInfo> infoFactory,
            DocumentWritingService documentWriter,
            BundledReaderService<MetadataDocument> documentBundleReader,
            PostProcessingService postProcessingService,
            GitRepoWrapper repoWrapper) {
        this.documentTypeLookupService = documentTypeLookupService;
        this.documentReader = documentReader;
        this.documentIdentifierService = documentIdentifierService;
        this.infoFactory = infoFactory;
        this.documentWriter = documentWriter;
        this.documentBundleReader = documentBundleReader;
        this.repo = repoWrapper;
    }
    
    public MetadataDocument read(String file) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        return documentBundleReader.readBundle(file);
    }
    
    public MetadataDocument read(String file, String revision) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        return documentBundleReader.readBundle(file, revision);
    }
    
    public MetadataDocument save(CatalogueUser user, InputStream inputStream, MediaType mediaType, String documentType, String message) throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException {
        Path tmpFile = Files.createTempFile("upload", null); //Create a temp file to upload the input stream to
        String id;
        MetadataDocument data;
        Class<? extends MetadataDocument> metadataType = documentTypeLookupService.getType(documentType);
        
        try {
            Files.copy(inputStream, tmpFile, StandardCopyOption.REPLACE_EXISTING); //copy the file so that we can pass over multiple times
            
            //the documentReader will close the underlying inputstream
            data = documentReader.read(Files.newInputStream(tmpFile), mediaType, metadataType); 
            MetadataInfo metadataInfo = createMetadataInfoWithDefaultPermissions(data, user, mediaType); //get the metadata info
            data.attachMetadata(metadataInfo);
            
            id = Optional.ofNullable(documentIdentifierService.generateFileId(data.getId()))
                             .orElse(documentIdentifierService.generateFileId());
            
            repo.save(user, id, message, metadataInfo, (o) -> Files.copy(tmpFile, o));
        }
        finally {
            Files.delete(tmpFile); //file no longer needed
        }
        
        if (data instanceof MetadataDocument) {
            data = save(user, (MetadataDocument)data, id, String.format("File upload for id: %s", id));
        }
        
        return data;
    }
    
    public MetadataDocument save(CatalogueUser user, MetadataDocument document, String message) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {       
        return save(user, 
            document,
            createMetadataInfoWithDefaultPermissions(document, user, MediaType.APPLICATION_JSON), 
            documentIdentifierService.generateFileId(),
            message
        );
    }
    
    public MetadataDocument save(CatalogueUser user, MetadataDocument document, String id, String message) throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException {
        return save(user,
            document, 
            retrieveMetadataInfoUpdatingRawType(id), 
            id, 
            message
        );
    }
    
    private MetadataDocument save(CatalogueUser user, MetadataDocument document, MetadataInfo metadataInfo, String id, String message) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        updateIdAndMetadataDate(document, id);
        String uri = documentIdentifierService.generateUri(id);
        addRecordUriAsResourceIdentifier(document, uri);
        document.attachUri(URI.create(uri));
        
        repo.save(user, id, message, metadataInfo,
            (o) -> documentWriter.write(document, MediaType.APPLICATION_JSON, o));
        
        return read(id);
    }
    
    public DataRevision<CatalogueUser> delete(CatalogueUser user, String id) throws DataRepositoryException {
        return repo.delete(user, id);
    }
    
    private MetadataInfo createMetadataInfoWithDefaultPermissions(MetadataDocument document, CatalogueUser user, MediaType mediaType) {
        MetadataInfo toReturn = infoFactory.createInfo(document, mediaType);
        String username = user.getUsername();
        toReturn.addPermission(Permission.VIEW, username);
        toReturn.addPermission(Permission.EDIT, username);
        toReturn.addPermission(Permission.DELETE, username);
        return toReturn;
    }
    
    private void updateIdAndMetadataDate(MetadataDocument document, String id) {
        
        document.setId(id).setMetadataDate(LocalDateTime.now());
    }
    
    private void addRecordUriAsResourceIdentifier(MetadataDocument document, String recordUri) {
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
    
     private MetadataInfo retrieveMetadataInfoUpdatingRawType(String id) throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException {
        MetadataInfo metadataInfo = documentBundleReader.readBundle(id).getMetadata();
        metadataInfo.setRawType(GEMINI_JSON_VALUE);
        return metadataInfo;
    }
}