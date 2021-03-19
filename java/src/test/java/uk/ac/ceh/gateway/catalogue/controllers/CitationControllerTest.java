package uk.ac.ceh.gateway.catalogue.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Citation;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.services.CitationService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CitationControllerTest {
    @Mock DocumentRepository documentRespository;
    @Mock CitationService citationService;
    private CitationController controller;
    
    @BeforeEach
    public void setUp() {
        controller = new CitationController(documentRespository, citationService);
    }
    
    @Test
    public void checkThatGettingCitationDelegatesToDocumentControllerREV() throws Exception {
        //Given
        GeminiDocument document = new GeminiDocument();
        String file = "file";
        String revision = "revision";
        when(documentRespository.read(file, revision))
                .thenReturn(document);
        when(citationService.getCitation(document)).thenReturn(
            Optional.ofNullable(Citation.builder().build())
        );
        
        //When
        controller.getCitation(CatalogueUser.PUBLIC_USER, file, revision);
        
        //Then
        verify(documentRespository).read(file, revision);
        verify(citationService).getCitation(document);
    }
    
    @Test
    public void checkThatGettingCitationDelegatesToDocumentController() throws Exception {
        //Given
        GeminiDocument document = new GeminiDocument();
        String file = "file";
        when(documentRespository.read(file)).thenReturn(document);
        when(citationService.getCitation(document)).thenReturn(
            Optional.ofNullable(Citation.builder().build())
        );
        
        //When
        controller.getCitation(CatalogueUser.PUBLIC_USER, file);
        
        //Then
        verify(documentRespository).read(file);
        verify(citationService).getCitation(document);
    }
    
    @Test
    public void checkThatNonGeminiDocumentsFailsToGetCitation() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            //Given
            MetadataDocument document = mock(MetadataDocument.class);

            //When
            controller.getCitation(document);

            //Then
            fail("Expected to get exception");
        });
    }
    
    @Test
    public void checkThatResourceNotFoundIfGeminiDocumentDoesntHaveCitation() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            //Given
            GeminiDocument document = new GeminiDocument();
            when(citationService.getCitation(document)).thenReturn(Optional.empty());

            //When
            controller.getCitation(document);

            //Then
            fail("Expected to get exception");
        });
    }
    
    @Test
    public void checkThatCitationCanBeObtained() {
        //Given
        Citation citation = Citation.builder().build();
        GeminiDocument document = mock(GeminiDocument.class);
        when(citationService.getCitation(document)).thenReturn(Optional.of(citation));
        
        //When
        Citation obtained = controller.getCitation(document);
        
        //Then
        assertEquals("Expected same citation", citation, obtained);
    }
}
