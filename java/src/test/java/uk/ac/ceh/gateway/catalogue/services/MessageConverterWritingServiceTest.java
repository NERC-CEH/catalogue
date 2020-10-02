package uk.ac.ceh.gateway.catalogue.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.ArrayList;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MessageConverterWritingServiceTest {
    @Mock private HttpMessageConverter converter;

    private MessageConverterWritingService service;
    
    @Before
    public void init() {
        service = new MessageConverterWritingService(new ArrayList<>());
    }
    
    @Test
    public void checkThatDelegatesWritingToConverter() throws Exception {
        //Given
        String test = "TestObject";
        when(converter.canWrite(String.class, MediaType.TEXT_HTML)).thenReturn(true);
        service.addMessageConverter(converter);
        
        //When
        service.write(test, MediaType.TEXT_HTML);
        
        //Then
        verify(converter).write(eq(test), eq(MediaType.TEXT_HTML), any());
    }
    
    @Test(expected=UnknownContentTypeException.class)
    public void checkThatThrowsExceptionIfNoConverterIsAMatch() throws Exception {
        //Given
        String test = "TestObject";
        
        //When
        service.write(test, MediaType.TEXT_HTML);
        
        //Then
        fail("Expected to fail");
    }
}
