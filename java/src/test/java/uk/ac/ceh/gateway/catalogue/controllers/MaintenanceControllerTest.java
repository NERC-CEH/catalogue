package uk.ac.ceh.gateway.catalogue.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkingException;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkingService;

/**
 *
 * @author jcoop, cjohn
 */
public class MaintenanceControllerTest {
    @Mock DocumentIndexingService indexService;
    @Mock DocumentLinkingService linkingService;
    
    private MaintenanceController controller;
    
    @Before
    public void createMaintenanceController() {
        MockitoAnnotations.initMocks(this);
        controller = new MaintenanceController(indexService, linkingService);
    }
    
    @Test
    public void checkThatReindexingDelegatesToIndexService() throws DocumentIndexingException {
        //Given
        //Nothing
        
        //When
        controller.reindexDocuments();
        
        //Then
        verify(indexService).rebuildIndex();
    }
    
    @Test
    public void checkThatReindexingDelegatesToLinkingService() throws DocumentLinkingException {
        //Given
        //Nothing
        
        //When
        controller.reindexLinks();
        
        //Then
        verify(linkingService).rebuildLinks();
    }
    
}
