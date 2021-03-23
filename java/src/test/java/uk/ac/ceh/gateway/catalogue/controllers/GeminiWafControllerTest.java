package uk.ac.ceh.gateway.catalogue.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;

public class GeminiWafControllerTest {
    @Mock(answer=RETURNS_DEEP_STUBS) DataRepository repo;
    @Mock(answer=RETURNS_DEEP_STUBS) MetadataListingService listingService;
    
    private GeminiWafController controller;
    
    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        controller = new GeminiWafController(repo, listingService);
    }
    
    
    @Test
    public void checkThatXmlExtensionIsAppendedToGeminiMetadataRecords() throws DataRepositoryException, IOException, PostProcessingException {
        //Given
        List<String> files = Arrays.asList("test1", "test2");
        List<String> resourceTypes = new ArrayList<>(Arrays.asList("dataset", "service"));
        when(repo.getLatestRevision().getRevisionID()).thenReturn("latest");
        when(listingService.getPublicDocuments("latest", GeminiDocument.class, resourceTypes)).thenReturn(files);
                
        //When
        ModelAndView modelAndView = controller.getWaf();
        
        //Then
        Map<String, Object> model = modelAndView.getModel();
        assertThat("Expected waf template", modelAndView.getViewName(), equalTo("/html/waf.ftl"));
        assertTrue("Expected to find files", model.containsKey("files"));
        
        List<String> filenames = (List<String>)model.get("files");
        assertThat("files contains files with extensions", filenames.contains("test1.xml"));
        assertThat("files contains files with extensions", filenames.contains("test2.xml"));
    }
    
    @Test
    public void checkThatGettingDocumentForwardsToDocumentsEndpoint() {
        //Given
        String id = "somerandomID";
        
        //When
        String springForward = controller.forwardToMetadata(id);
        
        //Then
        assertThat("Expected that forwards to correct location", springForward, equalTo("forward:/documents/somerandomID?format=gemini"));
    }
}
