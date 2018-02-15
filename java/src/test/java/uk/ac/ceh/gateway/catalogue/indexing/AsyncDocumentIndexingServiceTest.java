package uk.ac.ceh.gateway.catalogue.indexing;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import java.util.concurrent.ExecutorService;
import java.util.List;
import java.util.ArrayList;

public class AsyncDocumentIndexingServiceTest {
    @Mock ExecutorService executor;
    @Mock DocumentIndexingService proxy;
    private AsyncDocumentIndexingService service;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        service = new AsyncDocumentIndexingService(proxy, executor);
    }

    @Test
    public void checkThatIsEmptyCanReturnTrue() throws DocumentIndexingException {
        //Given
        when(proxy.isIndexEmpty()).thenReturn(true);
        
        //When
        boolean isEmpty = service.isIndexEmpty();

        //Then
        assertTrue("Expected to get a true value", isEmpty);
    }

    @Test
    public void checkThatIsEmptyCanReturnFalse() throws DocumentIndexingException {
        //Given
        when(proxy.isIndexEmpty()).thenReturn(false);
        
        //When
        boolean isEmpty = service.isIndexEmpty();

        //Then
        assertFalse("Expected to get a false value", isEmpty);
    }

    @Test
    public void checkThatReindexingGetsDelegated() throws DocumentIndexingException {
        //Given
        //Nothing

        //When
        service.rebuildIndex();

        //Then
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).submit(captor.capture());
        Runnable runnable = captor.getValue();
        runnable.run();
        verify(proxy).rebuildIndex();
    }

    @Test
    public void checkThatIndexingGetsDelegated() throws DocumentIndexingException {
        //Given
        List<String> toIndex = new ArrayList<>();
        String revision = "rev";

        //When
        service.indexDocuments(toIndex, revision);

        //Then
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).submit(captor.capture());
        Runnable runnable = captor.getValue();
        runnable.run();
        verify(proxy).indexDocuments(toIndex, revision);
    }

    @Test
    public void checkThatUnindexingGetsDelegated() throws DocumentIndexingException {
        //Given
        List<String> toUnIndex = new ArrayList<>();

        //When
        service.unindexDocuments(toUnIndex);

        //Then
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).submit(captor.capture());
        Runnable runnable = captor.getValue();
        runnable.run();
        verify(proxy).unindexDocuments(toUnIndex);
    }
}
