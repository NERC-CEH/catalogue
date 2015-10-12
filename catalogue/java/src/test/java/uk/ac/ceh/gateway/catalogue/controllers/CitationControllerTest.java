package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.model.Citation;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
public class CitationControllerTest {
    @Mock DocumentController documents;
    private CitationController controller;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = spy(new CitationController(documents));
    }
    
    @Test
    public void checkThatGettingCitationDelegatesToDocumentControllerREV() throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException {
        //Given
        MetadataDocument document = mock(MetadataDocument.class);
        String file = "file";
        String revision = "revision";
        doReturn(null).when(controller).getCitation(document);
        when(documents.readMetadata(CatalogueUser.PUBLIC_USER, file, revision))
                .thenReturn(document);
        
        //When
        controller.getCitation(CatalogueUser.PUBLIC_USER, file, revision);
        
        //Then
        verify(documents).readMetadata(CatalogueUser.PUBLIC_USER, file, revision);
    }
    
    @Test
    public void checkThatGettingCitationDelegatesToDocumentController() throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException {
        //Given
        MetadataDocument document = mock(MetadataDocument.class);
        String file = "file";
        doReturn(null).when(controller).getCitation(document);
        when(documents.readMetadata(CatalogueUser.PUBLIC_USER, file))
                .thenReturn(document);
        
        //When
        controller.getCitation(CatalogueUser.PUBLIC_USER, file);
        
        //Then
        verify(documents).readMetadata(CatalogueUser.PUBLIC_USER, file);
    }
    
    @Test(expected=ResourceNotFoundException.class)
    public void checkThatNonGeminiDocumentsFailsToGetCitation() {
        //Given
        MetadataDocument document = mock(MetadataDocument.class);
        
        //When
        controller.getCitation(document);
        
        //Then
        fail("Expected to get exception");
    }
    
    @Test(expected=ResourceNotFoundException.class)
    public void checkThatResourceNotFoundIfGeminiDocumentDoesntHaveCitation() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getCitation()).thenReturn(null);
        
        //When
        controller.getCitation(document);
        
        //Then
        fail("Expected to get exception");
    }
    
    @Test
    public void checkThatCitationCanBeObtained() {
        //Given
        Citation citation = Citation.builder().build();
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getCitation()).thenReturn(citation);
        
        //When
        Citation obtained = controller.getCitation(document);
        
        //Then
        assertEquals("Expected same citation", citation, obtained);
    }
}
