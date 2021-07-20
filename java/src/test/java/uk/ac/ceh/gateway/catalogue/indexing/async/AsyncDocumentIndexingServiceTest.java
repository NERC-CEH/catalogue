package uk.ac.ceh.gateway.catalogue.indexing.async;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AsyncDocumentIndexingServiceTest {
    @Mock ExecutorService executor;
    @Mock
    DocumentIndexingService proxy;
    @InjectMocks private AsyncDocumentIndexingService service;

    @Test
    public void checkThatIsEmptyCanReturnTrue() throws DocumentIndexingException {
        //Given
        when(proxy.isIndexEmpty()).thenReturn(true);

        //When
        boolean isEmpty = service.isIndexEmpty();

        //Then
        assertTrue(isEmpty);
    }

    @Test
    public void checkThatIsEmptyCanReturnFalse() throws DocumentIndexingException {
        //Given
        when(proxy.isIndexEmpty()).thenReturn(false);

        //When
        boolean isEmpty = service.isIndexEmpty();

        //Then
        assertFalse(isEmpty);
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
