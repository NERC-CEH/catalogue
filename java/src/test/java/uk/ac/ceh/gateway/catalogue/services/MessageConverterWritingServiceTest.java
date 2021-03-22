package uk.ac.ceh.gateway.catalogue.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.ArrayList;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MessageConverterWritingServiceTest {
    @Mock private HttpMessageConverter converter;

    private MessageConverterWritingService service;
    
    @BeforeEach
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
    
    @Test
    public void checkThatThrowsExceptionIfNoConverterIsAMatch() {
        Assertions.assertThrows(UnknownContentTypeException.class, () -> {
            //Given
            String test = "TestObject";

            //When
            service.write(test, MediaType.TEXT_HTML);

            //Then
            fail("Expected to fail");
        });
    }
}
