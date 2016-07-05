package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
public class DocumentControllerTest {
    
    @Mock DocumentRepository documentRepository;
    private DocumentController controller;
    
    @Before
    public void initMocks() throws IOException {
        MockitoAnnotations.initMocks(this);
        controller = new DocumentController(documentRepository);
    }
    
    @Test
    public void checkItCanRewriteIdToDocument() {
        //Given
        String id = "M3tADATA_ID";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("hostname");
        request.setPathInfo("id/" + id);
        request.setQueryString("query=string");
        
        //When
        RedirectView view = controller.redirectToResource(id, request);
        
        //Then
        assertThat(view.getUrl(), equalTo("http://hostname/documents/M3tADATA_ID?query=string"));
    }
     
    @Test
    public void checkCanUploadFile() throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException {
        //Given
        CatalogueUser user = new CatalogueUser();
        InputStream inputStream = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root></root>".getBytes());
        MediaType mediaType = MediaType.TEXT_XML;
        MultipartFile multipartFile = new MockMultipartFile("test", "test", MediaType.TEXT_XML_VALUE, inputStream);
        String documentType = "GEMINI_DOCUMENT";
        GeminiDocument document = new GeminiDocument();
        document.setUri(URI.create("https://catalogue.ceh.ac.uk/id/123-test"));
        String message = "new file upload";
        
        given(documentRepository.save(eq(user), any(), any(MediaType.class), eq(documentType), eq(message))).willReturn(document);
              
        //When
        controller.uploadDocument(user, multipartFile, documentType);
        
        //Then
        verify(documentRepository).save(eq(user), any(), eq(mediaType), eq(documentType), eq(message));
    }
    
    @Test
    public void checkCanCreateModelDocument() throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        //Given
        CatalogueUser user = new CatalogueUser();
        Model document = new Model();
        document.attachUri(URI.create("https://catalogue.ceh.ac.uk/id/123-test"));
        String message = "new Model Document";
        
        given(documentRepository.save(user, document, message)).willReturn(document);
              
        //When
        ResponseEntity<MetadataDocument> actual = controller.uploadModelDocument(user, document);
        
        //Then
        verify(documentRepository).save(user, document, message);
        assertThat("Should have 201 CREATED status", actual.getStatusCode(), equalTo(HttpStatus.CREATED));
    }
    
    @Test
    public void checkCanEditModelDocument() throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        //Given
        CatalogueUser user = mock(CatalogueUser.class);
        Model document = mock(Model.class);
        String fileId = "test";
        String message = "Edited document: test";
        
        given(documentRepository.save(user, document, fileId, message)).willReturn(document);
        given(document.getUri()).willReturn(URI.create("https://catalogue.ceh.ac.uk/id/123-test"));
              
        //When
        ResponseEntity<MetadataDocument> actual = controller.updateModelDocument(user, fileId, document);
        
        //Then
        verify(documentRepository).save(user, document, fileId, "Edited document: test");
        assertThat("Should have 200 OK status", actual.getStatusCode(), equalTo(HttpStatus.OK));
    }
    
    @Test
    public void checkCanCreateGeminiDocument() throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        //Given
        CatalogueUser user = new CatalogueUser();
        GeminiDocument document = new GeminiDocument();
        document.attachUri(URI.create("https://catalogue.ceh.ac.uk/id/123-test"));
        String message = "new Gemini Document";
        
        given(documentRepository.save(user, document, message)).willReturn(document);
              
        //When
        ResponseEntity<MetadataDocument> actual = controller.uploadDocument(user, document);
        
        //Then
        verify(documentRepository).save(user, document, message);
        assertThat("Should have 201 CREATED status", actual.getStatusCode(), equalTo(HttpStatus.CREATED));
    }
    
    @Test
    public void checkCanEditGeminiDocument() throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        //Given
        CatalogueUser user = mock(CatalogueUser.class);
        GeminiDocument document = mock(GeminiDocument.class);
        String fileId = "test";
        String message = "message";
        
        given(documentRepository.save(user, document, fileId, message)).willReturn(document);
        given(document.getUri()).willReturn(URI.create("https://catalogue.ceh.ac.uk/id/123-test"));
              
        //When
        ResponseEntity<MetadataDocument> actual = controller.updateDocument(user, fileId, document);
        
        //Then
        verify(documentRepository).save(user, document, fileId, "Edited document: test");
        assertThat("Should have 200 OK status", actual.getStatusCode(), equalTo(HttpStatus.OK));
    }
    
    @Test
    public void checkCanDeleteAFile() throws IOException {
        //Given
        CatalogueUser user = mock(CatalogueUser.class);
        
        //When
        controller.deleteDocument(user, "id");
        
        //Then
        verify(documentRepository).delete(user, "id");
    }
    
    @Test
    public void checkCanReadDocumentAtRevision() throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException {
        //Given
        CatalogueUser user = mock(CatalogueUser.class);
        String file = "myFile";       
        String latestRevisionId = "latestRev";
        
        //When
        controller.readMetadata(user, file, latestRevisionId);
        
        //Then
        verify(documentRepository).read(file, latestRevisionId);
    }
    
    @Test
    public void checkCanReadDocumentLatestRevision() throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException {
        //Given
        CatalogueUser user = mock(CatalogueUser.class);
        String file = "myFile";
        
        //When
        controller.readMetadata(user, file);
        
        //Then
        verify(documentRepository).read(file);
    }
}
