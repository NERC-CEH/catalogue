package uk.ac.ceh.gateway.catalogue.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.MapServerIndexingService;
import uk.ac.ceh.gateway.catalogue.model.MaintenanceResponse;
import uk.ac.ceh.gateway.catalogue.services.DataRepositoryOptimizingService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MaintenanceControllerTest {
    @Mock(answer=RETURNS_DEEP_STUBS) DataRepositoryOptimizingService repoService;
    @Mock DocumentIndexingService indexService;
    @Mock DocumentIndexingService linkingService;
    @Mock DocumentIndexingService validationService;
    @Mock MapServerIndexingService mapserverService;
    
    private MaintenanceController controller;
    
    @BeforeEach
    public void createMaintenanceController() {
        controller = new MaintenanceController(repoService, indexService, linkingService, validationService, mapserverService);
    }
    
    @Test
    public void checkThatReindexingDelegatesToIndexService() throws DocumentIndexingException {
        //Given
        //Nothing
        //When
        HttpEntity<MaintenanceResponse> reindexDocuments = controller.reindexDocuments();
        
        //Then
        verify(indexService).rebuildIndex();
    }
    
    @Test
    public void checkThatRecreatingMapfilesDelegatesToMapServerService() throws DocumentIndexingException {
        //Given
        //Nothing
        //When
        HttpEntity<MaintenanceResponse> reindexDocuments = controller.recreateMapFiles();
        
        //Then
        verify(mapserverService).rebuildIndex();
    }
    
    @Test
    public void checkThatReindexingDelegatesToLinkingService() throws DocumentIndexingException {
        //Given
        //Nothing
        
        //When
        controller.reindexLinks();
        
        //Then
        verify(linkingService).rebuildIndex();
    }
    
    @Test
    public void chechThatCanOptimizeGitRepository() throws DataRepositoryException {
        //Given
        //Nothing
        
        //When
        controller.optimizeRepository();
        
        //Then
        verify(repoService).performOptimization();
    }
    
    @Test
    public void checkThatReindexingDelegatesToValidationService() throws DocumentIndexingException {
        //Given
        //Nothing
        
        //When
        controller.validateRepository();
        
        //Then
        verify(validationService).rebuildIndex();
    }
    
    @Test
    public void checkThatCanLoadMaintenancePageWhenThereRepoIsBroken() throws DataRepositoryException {
        //Given
        String errorMessage = "Something has gone wrong";
        when(repoService.getLatestRevision()).thenThrow(new DataRepositoryException(errorMessage));
        
        //When
        MaintenanceResponse response = controller.loadMaintenancePage();
        
        //Then
        assertThat("Expected one message", response.getMessages().size(), equalTo(1));
        assertThat("Expected message to exist", response.getMessages().contains(errorMessage));
    }

    @Test
    public void checkThatCanLoadMaintenancePageWhenIndexingIsBroken() throws DocumentIndexingException {
        //Given
        String errorMessage = "Something has gone wrong";
        when(indexService.isIndexEmpty()).thenThrow(new DocumentIndexingException(errorMessage));
        
        //When
        MaintenanceResponse response = controller.loadMaintenancePage();
        
        //Then
        assertThat("Expected one message", response.getMessages().size(), equalTo(1));
        assertThat("Expected message to exist", response.getMessages().contains(errorMessage));
    }
}