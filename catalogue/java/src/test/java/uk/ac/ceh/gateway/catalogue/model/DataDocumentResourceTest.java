package uk.ac.ceh.gateway.catalogue.model;

import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataDocument;

/**
 *
 * @author cjohn
 */
public class DataDocumentResourceTest {
    @Mock DataDocument dataDocument;
    
    @Before
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
        assertTrue("Expected all data document resources to exists", exists);
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
        assertEquals("Expectet the same content legnth", contentLength, resourceLength);
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
        assertSame("Expectet the same inputstream", stream, resourceStream);
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
        assertEquals("Expected concat of rev and filename", "revfile", description);
    }
}
