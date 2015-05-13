package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.DATACITE_XML_VALUE;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
public class DataciteControllerTest {
    @Mock(answer=RETURNS_DEEP_STUBS) DataRepository<CatalogueUser> repo;
    @Mock(answer=RETURNS_DEEP_STUBS) BundledReaderService<MetadataDocument> documentBundleReader;
    
    private DataciteController controller;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        controller = new DataciteController(repo, documentBundleReader);
    }
    
    @Test
    public void checkThatGeminiDocumentCanBeDatacited() throws DataRepositoryException, UnknownContentTypeException, IOException {
        //Given
        String revision = "rev";
        String id = "id";
        GeminiDocument document = mock(GeminiDocument.class);
        Keyword keyword = Keyword.builder().value("nonGeographicDataset").build();
        when(repo.getLatestRevision().getRevisionID()).thenReturn(revision);
        when(documentBundleReader.readBundle(id, revision)).thenReturn(document);
        when(document.getResourceType()).thenReturn(keyword);
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        //When
        ModelAndView modelAndView = controller.getDataciteRequest(response, id);
        
        //Then
        Map<String, Object> model = modelAndView.getModel();
        assertEquals("Expected document to be in model", model.get("doc"), document);
        assertEquals("Expected resource type to be Dataset", model.get("resourceType"), "Dataset");
        assertEquals("Expected the datacite template", modelAndView.getViewName(), "/datacite/datacite.xml.tpl");
        verify(response).setContentType(DATACITE_XML_VALUE);
    }
    
    @Test(expected=ResourceNotFoundException.class)
    public void checkThatNonGeminiDocumentCantBeDatacited() throws DataRepositoryException, UnknownContentTypeException, IOException {
        //Given
        String revision = "rev";
        String id = "id";
        MetadataDocument document = mock(MetadataDocument.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(repo.getLatestRevision().getRevisionID()).thenReturn(revision);
        when(documentBundleReader.readBundle(id, revision)).thenReturn(document);
        
        //When
        controller.getDataciteRequest(response, id);
        
        //Then
        fail("Expected to fail with exception");
    }    
}
