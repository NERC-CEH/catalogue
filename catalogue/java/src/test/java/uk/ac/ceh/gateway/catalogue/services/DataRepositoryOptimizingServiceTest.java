package uk.ac.ceh.gateway.catalogue.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.git.GitDataRepository;

/**
 *
 * @author cjohn
 */
public class DataRepositoryOptimizingServiceTest {
    
    @Test
    public void checkThatDateGetsSetOnOptimization() throws DataRepositoryException {
        //Given
        GitDataRepository repo = mock(GitDataRepository.class);
        DataRepositoryOptimizingService service = new DataRepositoryOptimizingService(repo);
        
        //When
        service.performOptimization();
        
        //Then
        assertNotNull(service.getLastOptimized());
    }
    
    @Test
    public void checkThatDateDoesntPersistIfNotUsingAGitRepo()throws DataRepositoryException {
        //Given
        DataRepository repo = mock(DataRepository.class);
        DataRepositoryOptimizingService service = new DataRepositoryOptimizingService(repo);
        
        //When
        service.performOptimization();
        
        //Then
        assertNull(service.getLastOptimized());
    }
    
    @Test
    public void checkThatOptimizesWhenCalled() throws DataRepositoryException {
        //Given
        GitDataRepository repo = mock(GitDataRepository.class);
        DataRepositoryOptimizingService service = new DataRepositoryOptimizingService(repo);
        
        //When
        service.performOptimization();
        
        //Then
        verify(repo).optimize();
    }
}
