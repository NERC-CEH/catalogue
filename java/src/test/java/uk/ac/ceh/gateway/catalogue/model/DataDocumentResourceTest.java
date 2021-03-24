package uk.ac.ceh.gateway.catalogue.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataDocument;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataDocumentResourceTest {
    @Mock DataDocument dataDocument;
    
    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void checkDataDocumentResourceExists() {
        //Given
        DataDocumentResource resource = new DataDocumentResource(dataDocument);
        
        //When
        boolean exists = resource.exists();
        
        //Then
        assertTrue(exists);
    }
    
    @Test
    public void checkThatContentLengthIsDelegated() throws IOException {
        //Given
        long contentLength = 500L;
        DataDocumentResource resource = new DataDocumentResource(dataDocument);
        
        when(dataDocument.length()).thenReturn(contentLength);
        
        //When
        long resourceLength = resource.contentLength();
        
        //Then
        assertEquals(contentLength, resourceLength);
    }
    
    @Test
    public void checkThatInputStreamIsDelegated() throws IOException {
        //Given
        InputStream stream = mock(InputStream.class);
        DataDocumentResource resource = new DataDocumentResource(dataDocument);
        
        when(dataDocument.getInputStream()).thenReturn(stream);
        
        //When
        InputStream resourceStream = resource.getInputStream();
        
        //Then
        assertSame(stream, resourceStream);
    }
    
    @Test
    public void checkCanCreateDescriptionFromDataDocument() {
        //Given
        DataDocumentResource resource = new DataDocumentResource(dataDocument);
        when(dataDocument.getFilename()).thenReturn("file");
        when(dataDocument.getRevision()).thenReturn("rev");
        
        //When
        String description = resource.getDescription();
        
        //Then
        assertEquals("revfile", description);
    }
}
