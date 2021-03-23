package uk.ac.ceh.gateway.catalogue.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.services.DocumentWritingService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MediaTypeValidatorTest {
    @Mock DocumentWritingService writer;
    
    @BeforeEach
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
        assertEquals(result.getWorstLevel(), ValidationLevel.ERROR);
    }
    
    @Test
    public void checkThatReturnsAValidValidationIfCanRead() throws IOException {
        //Given
        MediaTypeValidator validator = new MediaTypeValidator("test", MediaType.ALL, writer);
        InputStream in = new ByteArrayInputStream(new byte[]{'a', 'b', 'c'});
        
        //When
        ValidationResult result = validator.validate(in);
        
        //Then
        assertEquals(result.getWorstLevel(), ValidationLevel.VALID);
    }
}
