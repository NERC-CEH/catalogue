package uk.ac.ceh.gateway.catalogue.services;

import java.io.InputStream;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

/**
 *
 * @author cjohn
 */
public class MessageConverterWritingServiceTest {
    private MessageConverterWritingService service;
    
    @Before
    public void init() {
        service = new MessageConverterWritingService();
    }
    
    @Test
    public void checkThatDelegatesWritingToConverter() throws Exception {
        //Given
        String test = "TestObject";
        HttpMessageConverter converter = mock(HttpMessageConverter.class);
        when(converter.canWrite(String.class, MediaType.TEXT_HTML)).thenReturn(true);
        service.addMessageConverter(converter);
        
        //When
        InputStream in = service.write(test, MediaType.TEXT_HTML);
        
        //Then
        verify(converter).write(eq(test), eq(MediaType.TEXT_HTML), any());
    }
    
    @Test(expected=UnknownContentTypeException.class)
    public void checkThatThrowsExceptionIfNoConverterIsAMatch() throws Exception {
        //Given
        String test = "TestObject";
        HttpMessageConverter converter = mock(HttpMessageConverter.class);
        
        //When
        InputStream in = service.write(test, MediaType.TEXT_HTML);
        
        //Then
        fail("Expected to fail");
    }
}
