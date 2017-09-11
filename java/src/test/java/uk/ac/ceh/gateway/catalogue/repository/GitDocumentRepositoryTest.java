package uk.ac.ceh.gateway.catalogue.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import org.junit.Before;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DocumentWritingService;

public class GitDocumentRepositoryTest {
    @Mock DocumentIdentifierService documentIdentifierService;
    @Mock DocumentReadingService documentReader;
    @Mock DocumentInfoMapper documentInfoMapper;
    @Mock BundledReaderService<MetadataDocument> documentBundleReader;
    @Mock PostProcessingService postProcessingService;
    @Mock DocumentWritingService documentWritingService;
    @Mock ObjectMapper mapper;
    @Mock DocumentTypeLookupService documentTypeLookupService;
    @Mock GitRepoWrapper repo;
    
    private GitDocumentRepository documentRepository;
    
    @Before
    public void initMocks() throws IOException {
        MockitoAnnotations.initMocks(this);
        documentRepository = new GitDocumentRepository(
                            documentTypeLookupService, 
                            documentReader,
                            documentIdentifierService,
                            documentWritingService,
                            documentBundleReader,   
                            postProcessingService,
                            repo);
    }
    
    @Test
    public void readLatestDocument() throws Exception {        
        //When
        documentRepository.read("file");
        
        //Then
        verify(documentBundleReader).readBundle("file");
    }
    
    @Test
    public void readDocumentAtRevision() throws Exception {       
        //When
        documentRepository.read("file", "special");
        
        //Then
        verify(documentBundleReader).readBundle("file", "special");
    }

    @Test
    public void savingMultipartFileStoresInputStreamIntoRepo() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser().setUsername("test").setEmail("test@example.com");
        InputStream inputStream = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root></root>".getBytes());
        String documentType = "GEMINI_DOCUMENT";
        String message = "message";
        GeminiDocument document = new GeminiDocument();
        String catalogue = "ceh";
        
        given(documentReader.read(any(), any(), any())).willReturn(document);
        given(documentIdentifierService.generateFileId()).willReturn("test");
        given(documentIdentifierService.generateUri("test")).willReturn("http://localhost:8080/id/test");
        given(documentBundleReader.readBundle(eq("test"))).willReturn(document);
       
        //When
        documentRepository.save(user, inputStream, MediaType.TEXT_XML, documentType, catalogue, message);
        
        //Then
        verify(repo).save(eq(user), eq("test"), eq(message), any(MetadataInfo.class), any());
        verify(repo).save(eq(user), eq("test"), eq("File upload for id: test"), any(MetadataInfo.class), any());
    }
    
    @Test
    public void saveNewGeminiDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser().setUsername("test").setEmail("test@example.com");
        GeminiDocument document = new GeminiDocument();
        String message = "new Gemini document";
        String catalogue = "test";
        
        given(documentIdentifierService.generateFileId()).willReturn("test");
        given(documentIdentifierService.generateUri("test")).willReturn("http://localhost:8080/id/test");
       
        //When
        documentRepository.saveNew(user, document, catalogue, message);
        
        //Then
        verify(repo).save(eq(user), eq("test"), eq("new Gemini document"), any(MetadataInfo.class), any());
    }
    
    @Test
    public void saveEditedGeminiDocument() throws Exception {
        //Given
        String id = "tulips";
        CatalogueUser user = new CatalogueUser().setUsername("test").setEmail("test@example.com");
        MetadataInfo metadataInfo = MetadataInfo.builder().build();
        MetadataDocument incomingDocument = new GeminiDocument()
            .setMetadata(metadataInfo);
        String message = "message";
        
        given(documentIdentifierService.generateUri(id)).willReturn("http://localhost:8080/id/test");
        
        //When
        documentRepository.save(user, incomingDocument, "tulips", message);
        
        //Then
        verify(repo).save(eq(user), eq(id), eq(message), any(MetadataInfo.class), any());
    }
    
    @Test
    public void checkCanDeleteAFile() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser().setUsername("test").setEmail("test@example.com");        
        
        //When
        documentRepository.delete(user, "id");
        
        //Then
        verify(repo).delete(user, "id");
    }
    
    @Test
    public void checkMetadataInfoUpdated() throws Exception {
        //Given
        CatalogueUser editor = new CatalogueUser()
            .setUsername("editor")
            .setEmail("editor@example.com");
        String file = "3c25e9b7-d3dd-41be-ae29-e8979bb462a2";
        String message = "Test message";
        MetadataInfo metadataInfo = MetadataInfo.builder()
            .catalogue("eidc")
            .documentType("MODEL_DOCUMENT")
            .rawType("application/json")
            .state("published")
            .build();
        metadataInfo.addPermission(Permission.EDIT, "editor");
        MetadataDocument document = new Model()
            .setId(file)
            .setMetadata(metadataInfo);        
        
        given(documentIdentifierService.generateUri(file)).willReturn("https://catalogue.ceh.ac.uk/id/3c25e9b7-d3dd-41be-ae29-e8979bb462a2");
        
        //When
        documentRepository.save(editor, document, file, message);
        
        //Then 
        verify(repo).save(eq(editor), eq(file), eq(message), eq(metadataInfo), any());
        
    }
    
}