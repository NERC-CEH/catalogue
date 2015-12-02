package uk.ac.ceh.gateway.catalogue.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import org.junit.Before;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
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

public class DocumentRepositoryTest {
    @Mock DocumentIdentifierService documentIdentifierService;
    @Mock DocumentReadingService documentReader;
    @Mock DocumentInfoMapper documentInfoMapper;
    @Mock DocumentInfoFactory<MetadataDocument, MetadataInfo> infoFactory;
    @Mock BundledReaderService<MetadataDocument> documentBundleReader;
    @Mock PostProcessingService postProcessingService;
    @Mock DocumentWritingService documentWritingService;
    @Mock ObjectMapper mapper;
    @Mock DocumentTypeLookupService documentTypeLookupService;
    @Mock GitRepoWrapper repo;
    
    private DocumentRepository documentRepository;
    
    @Before
    public void initMocks() throws IOException {
        MockitoAnnotations.initMocks(this);
        documentRepository = new DocumentRepository(
                            documentTypeLookupService, 
                            documentReader,
                            documentIdentifierService,
                            infoFactory,
                            documentWritingService,
                            documentBundleReader,   
                            postProcessingService,
                            repo);
    }
    
    @Test
    public void readLatestDocument() throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {        
        //When
        documentRepository.read("file");
        
        //Then
        verify(documentBundleReader).readBundle("file");
    }
    
    @Test
    public void readDocumentAtRevision() throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {       
        //When
        documentRepository.read("file", "special");
        
        //Then
        verify(documentBundleReader).readBundle("file", "special");
    }

    @Test
    public void savingMultipartFileStoresInputStreamIntoRepo() throws IOException, UnknownContentTypeException, DataRepositoryException, DocumentIndexingException, PostProcessingException {
        //Given
        CatalogueUser user = new CatalogueUser().setUsername("test").setEmail("test@example.com");
        MultipartFile multipartFile = new MockMultipartFile("file", null, MediaType.TEXT_XML_VALUE, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root></root>".getBytes());
        String type = "GEMINI_DOCUMENT";
        GeminiDocument document = new GeminiDocument();
        MetadataInfo metadataInfo = new MetadataInfo();
        
        given(documentReader.read(any(), any(), any())).willReturn(document);
        given(infoFactory.createInfo(any(), any())).willReturn(metadataInfo);
        given(documentIdentifierService.generateFileId()).willReturn("test");
        given(documentIdentifierService.generateUri("test")).willReturn("http://localhost:8080/id/test");
        given(documentBundleReader.readBundle(eq("test"))).willReturn(document);
       
        //When
        documentRepository.save(user, multipartFile, type);
        
        //Then
        verify(repo).save(eq(user), eq("test"), eq("new Gemini XML document: %s"), eq(metadataInfo), any());
        verify(repo).save(eq(user), eq("test"), eq("edit Gemini document: %s"), eq(metadataInfo), any());
    }
    
    @Test
    public void saveNewGeminiDocument() throws IOException, UnknownContentTypeException, DataRepositoryException, DocumentIndexingException, PostProcessingException {
        //Given
        CatalogueUser user = new CatalogueUser().setUsername("test").setEmail("test@example.com");
        GeminiDocument document = new GeminiDocument();
        MetadataInfo metadataInfo = new MetadataInfo();
        
        given(infoFactory.createInfo(document, MediaType.APPLICATION_JSON)).willReturn(metadataInfo);
        given(documentIdentifierService.generateFileId()).willReturn("test");
        given(documentIdentifierService.generateUri("test")).willReturn("http://localhost:8080/id/test");
       
        //When
        documentRepository.save(user, document);
        
        //Then
        verify(repo).save(eq(user), eq("test"), eq("new Gemini document: %s"), eq(metadataInfo), any());
    }
    
    @Test
    public void saveEditedGeminiDocument() throws IOException, UnknownContentTypeException, DataRepositoryException, DocumentIndexingException, PostProcessingException {
        //Given
        String id = "tulips";
        CatalogueUser user = new CatalogueUser().setUsername("test").setEmail("test@example.com");
        GeminiDocument incomingDocument = new GeminiDocument();
        MetadataInfo metadataInfo = new MetadataInfo();
        GeminiDocument retrieved = new GeminiDocument();
        retrieved.setMetadata(metadataInfo);
        
        given(documentIdentifierService.generateUri(id)).willReturn("http://localhost:8080/id/test");
        given(documentBundleReader.readBundle(id)).willReturn(retrieved);
        
        //When
        documentRepository.save(user, incomingDocument, "tulips");
        
        //Then
        verify(repo).save(eq(user), eq(id), eq("edit Gemini document: %s"), eq(metadataInfo), any());
    }
    
    @Test
    public void checkCanDeleteAFile() throws IOException {
        //Given
        CatalogueUser user = new CatalogueUser().setUsername("test").setEmail("test@example.com");        
        
        //When
        documentRepository.delete(user, "id");
        
        //Then
        verify(repo).delete(user, "id");
    }
    
}