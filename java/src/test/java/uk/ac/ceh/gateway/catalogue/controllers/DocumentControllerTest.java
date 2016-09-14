package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.LinkDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

/**
 *
 * @author cjohn
 */
public class DocumentControllerTest {
    
    @Mock DocumentRepository documentRepository;
    private DocumentController controller;
    private final String linkedDocumentId = "0a6c7c4c-0515-40a8-b84e-7ffe622b2579";
    
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
    public void checkCanUploadFile() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser();
        InputStream inputStream = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root></root>".getBytes());
        MediaType mediaType = MediaType.TEXT_XML;
        MultipartFile multipartFile = new MockMultipartFile("test", "test", MediaType.TEXT_XML_VALUE, inputStream);
        String documentType = "GEMINI_DOCUMENT";
        GeminiDocument document = new GeminiDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String message = "new file upload";
        String catalogue = "catalogue";
        
        given(documentRepository.save(eq(user), any(), any(MediaType.class), eq(documentType), eq(catalogue), eq(message))).willReturn(document);
              
        //When
        controller.uploadFile(user, multipartFile, documentType, catalogue);
        
        //Then
        verify(documentRepository).save(eq(user), any(), eq(mediaType), eq(documentType), eq(catalogue), eq(message));
    }
    
    @Test
    public void checkCanCreateModelDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser();
        Model document = new Model();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String message = "new Model Document";
        String catalogue = "catalogue";
        
        given(documentRepository.saveNew(user, document, catalogue, message)).willReturn(document);
              
        //When
        ResponseEntity<MetadataDocument> actual = controller.uploadModelDocument(user, document, catalogue);
        
        //Then
        verify(documentRepository).saveNew(user, document, catalogue, message);
        assertThat("Should have 201 CREATED status", actual.getStatusCode(), equalTo(HttpStatus.CREATED));
    }
    
    @Test
    public void checkCanEditModelDocument() throws Exception {
        //Given
        CatalogueUser user = mock(CatalogueUser.class);
        Model document = mock(Model.class);
        String fileId = "test";
        String message = "Edited document: test";
        
        given(documentRepository.save(user, document, fileId, message)).willReturn(document);
        given(document.getUri()).willReturn("https://catalogue.ceh.ac.uk/id/123-test");
              
        //When
        ResponseEntity<MetadataDocument> actual = controller.updateModelDocument(user, fileId, document);
        
        //Then
        verify(documentRepository).save(user, document, fileId, "Edited document: test");
        assertThat("Should have 200 OK status", actual.getStatusCode(), equalTo(HttpStatus.OK));
    }
    
    @Test
    public void checkCanCreateGeminiDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser();
        GeminiDocument document = new GeminiDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String message = "new Gemini Document";
        String catalogue = "catalogue";
        
        given(documentRepository.saveNew(user, document, catalogue, message)).willReturn(document);
              
        //When
        ResponseEntity<MetadataDocument> actual = controller.uploadGeminiDocument(user, document, catalogue);
        
        //Then
        verify(documentRepository).saveNew(user, document, catalogue, message);
        assertThat("Should have 201 CREATED status", actual.getStatusCode(), equalTo(HttpStatus.CREATED));
    }
    
    @Test
    public void checkCanEditGeminiDocument() throws Exception {
        //Given
        String fileId = "test";
        String message = "message";
        CatalogueUser user = new CatalogueUser();
        MetadataDocument document = new GeminiDocument()
            .setId(fileId)
            .setUri("https://catalogue.ceh.ac.uk/id/123-test")
            .setMetadata(MetadataInfo.builder().build());
        
        given(documentRepository.save(user, document, fileId, message)).willReturn(document);
              
        //When
        ResponseEntity<MetadataDocument> actual = controller.updateGeminiDocument(user, fileId, (GeminiDocument) document);
        
        //Then
        verify(documentRepository).save(user, document, fileId, "Edited document: test");
        assertThat("Should have 200 OK status", actual.getStatusCode(), equalTo(HttpStatus.OK));
    }
    
    @Test
    public void checkCanCreateLinkedDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser();
        LinkDocument document = LinkDocument.builder().linkedDocumentId(linkedDocumentId).build();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String message = "new Linked Document";
        String catalogue = "catalogue";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("catalogue.ceh.ac.uk");
        
        given(documentRepository.saveNew(user, document, catalogue, message)).willReturn(document);
              
        //When
        ResponseEntity<MetadataDocument> actual = controller.uploadLinkedDocument(user, document, catalogue);
        
        //Then
        verify(documentRepository).saveNew(user, document, catalogue, message);
        assertThat("Should have 201 CREATED status", actual.getStatusCode(), equalTo(HttpStatus.CREATED));
    }
    
    @Test
    public void checkCanEditLinkedDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser();
        LinkDocument document = LinkDocument.builder().linkedDocumentId(linkedDocumentId).build();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String fileId = "test";
        String message = "message";
        
        given(documentRepository.save(user, document, fileId, message)).willReturn(document);
              
        //When
        ResponseEntity<MetadataDocument> actual = controller.updateLinkedDocument(user, fileId, document);
        
        //Then
        verify(documentRepository).save(user, document, fileId, "Edited document: test");
        assertThat("Should have 200 OK status", actual.getStatusCode(), equalTo(HttpStatus.OK));
    }
    
    @Test
    public void checkCanDeleteAFile() throws Exception {
        //Given
        CatalogueUser user = mock(CatalogueUser.class);
        
        //When
        controller.deleteDocument(user, "id");
        
        //Then
        verify(documentRepository).delete(user, "id");
    }
    
    @Test
    public void checkCanReadDocumentAtRevision() throws Exception {
        //Given
        CatalogueUser user = CatalogueUser.PUBLIC_USER;
        String file = "myFile";       
        String latestRevisionId = "latestRev";
        MetadataInfo info = MetadataInfo.builder().build();
        MetadataDocument document = mock(MetadataDocument.class);
        given(document.getMetadata()).willReturn(info);
        given(documentRepository.read(file, latestRevisionId))
            .willReturn(document);
        
        //When
        controller.readMetadata(user, file, latestRevisionId);
        
        //Then
        verify(documentRepository).read(file, latestRevisionId);
    }
    
    @Test
    public void checkCanReadDocumentLatestRevision() throws Exception {
        //Given
        CatalogueUser user = CatalogueUser.PUBLIC_USER;
        String file = "myFile";
        MetadataInfo info = MetadataInfo.builder().build();
        MetadataDocument document = mock(MetadataDocument.class);
        given(document.getMetadata()).willReturn(info);
        given(documentRepository.read(file))
            .willReturn(document);
        
        //When
        controller.readMetadata(user, file);
        
        //Then
        verify(documentRepository).read(file);
    }
}
