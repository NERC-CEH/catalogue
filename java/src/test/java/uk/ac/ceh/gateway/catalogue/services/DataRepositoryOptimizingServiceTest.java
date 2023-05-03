package uk.ac.ceh.gateway.catalogue.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DataRepositoryOptimizingServiceTest {

    @Mock
    private GitDataRepository<CatalogueUser> repo;
    private DataRepositoryOptimizingService service;

    @BeforeEach
    public void setup() {
        this.service = new DataRepositoryOptimizingService(this.repo);
    }
    
    @Test
    public void checkThatDateGetsSetOnOptimization() throws DataRepositoryException {
        //When
        service.performOptimization();
        
        //Then
        assertNotNull(service.getLastOptimized());
    }
    
    @Test
    public void checkThatDateDoesntPersistIfNotUsingAGitRepo()throws DataRepositoryException {
        //Given
        DataRepository<CatalogueUser> repo = mock(DataRepository.class); // Not a Git repository
        DataRepositoryOptimizingService service = new DataRepositoryOptimizingService(repo);
        
        //When
        service.performOptimization();
        
        //Then
        assertNull(service.getLastOptimized());
    }
    
    @Test
    public void checkThatOptimizesWhenCalled() throws DataRepositoryException {
        //When
        service.performOptimization();
        
        //Then
        verify(repo).optimize();
    }
}
