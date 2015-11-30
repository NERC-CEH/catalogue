package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
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
    public void checkCanUploadFile() throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException {
        //Given
        CatalogueUser user = mock(CatalogueUser.class);
        MultipartFile multipartFile = mock(MultipartFile.class);
        String type = "GEMINI_DOCUMENT";
        GeminiDocument document = mock(GeminiDocument.class);
        
        given(documentRepository.save(user, multipartFile, type)).willReturn(document);
        given(document.getUri()).willReturn(URI.create("https://catalogue.ceh.ac.uk/id/123-test"));
              
        //When
        controller.uploadDocument(user, multipartFile, type);
        
        //Then
        verify(documentRepository).save(user, multipartFile, type);
    }
    
    @Test
    public void checkCanCreateGeminiDocument() throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        //Given
        CatalogueUser user = mock(CatalogueUser.class);
        GeminiDocument document = mock(GeminiDocument.class);
        
        given(documentRepository.save(user, document)).willReturn(document);
        given(document.getUri()).willReturn(URI.create("https://catalogue.ceh.ac.uk/id/123-test"));
              
        //When
        controller.uploadDocument(user, document);
        
        //Then
        verify(documentRepository).save(user, document);
    }
    
    @Test
    public void checkCanEditGeminiDocument() throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        //Given
        CatalogueUser user = mock(CatalogueUser.class);
        GeminiDocument document = mock(GeminiDocument.class);
        String fileId = "test";
        
        given(documentRepository.save(user, document)).willReturn(document);
        given(document.getUri()).willReturn(URI.create("https://catalogue.ceh.ac.uk/id/123-test"));
              
        //When
        controller.updateDocument(user, fileId, document);
        
        //Then
        verify(documentRepository).save(user, document, fileId);
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
