package uk.ac.ceh.gateway.catalogue.validation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.services.DocumentWritingService;

/**
 *
 * @author cjohn
 */
public class MediaTypeValidatorTest {
    @Mock DocumentWritingService writer;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void checkThatReturnsABadValidationIfCantRead() throws IOException {
        //Given
        MediaTypeValidator validator = new MediaTypeValidator("test", MediaType.ALL, writer);
        InputStream in = mock(InputStream.class);
        when(in.read(any(byte[].class))).thenThrow(new IOException("You can't read me!"));
        
        //When
        ValidationResult result = validator.validate(in);
        
        //Then
        assertEquals("Expected to have failed", result.getWorstLevel(), ValidationLevel.ERROR);
    }
    
    @Test
    public void checkThatReturnsAValidValidationIfCanRead() throws IOException {
        //Given
        MediaTypeValidator validator = new MediaTypeValidator("test", MediaType.ALL, writer);
        InputStream in = new ByteArrayInputStream(new byte[]{'a', 'b', 'c'});
        
        //When
        ValidationResult result = validator.validate(in);
        
        //Then
        assertEquals("Expected to have failed", result.getWorstLevel(), ValidationLevel.VALID);
    }
}
