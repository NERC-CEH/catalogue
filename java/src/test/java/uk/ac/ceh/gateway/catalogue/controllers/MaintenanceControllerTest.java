package uk.ac.ceh.gateway.catalogue.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;

/**
 *
 * @author jcoop, cjohn
 */
public class MaintenanceControllerTest {
    @Mock DocumentIndexingService indexService;
    
    private MaintenanceController controller;
    
    @Before
    public void createMaintenanceController() {
        MockitoAnnotations.initMocks(this);
        controller = new MaintenanceController(indexService);
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
    
}
