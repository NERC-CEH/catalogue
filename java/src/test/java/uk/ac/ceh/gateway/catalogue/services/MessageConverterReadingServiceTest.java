package uk.ac.ceh.gateway.catalogue.services;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
@ExtendWith(MockitoExtension.class)
class MessageConverterReadingServiceTest {
    private MessageConverterReadingService messageConverterReadingService;
    
    @BeforeEach
    void createMessageConverter() {
        messageConverterReadingService = new MessageConverterReadingService();
    }
    
    @Test
    void checkThatUnknownContentTypeExceptionIsThrownIfMessageConverterIsNotPresent() {

        //Given
        InputStream in = mock(InputStream.class);

        //When
        assertThrows(UnknownContentTypeException.class, () -> {
            messageConverterReadingService.read(in, MediaType.TEXT_HTML, Object.class);
        });
    }
    
    @Test
    @SneakyThrows
    public void checkThatFirstMatchingMessageConverterReturns() {
        //Given
        InputStream in = mock(InputStream.class);
        HttpMessageConverter converter1 = mock(HttpMessageConverter.class);
        HttpMessageConverter converter2 = mock(HttpMessageConverter.class);
        
        when(converter1.canRead(Object.class, MediaType.TEXT_XML)).thenReturn(Boolean.TRUE);
        when(converter1.read(eq(Object.class), any(HttpInputMessage.class))).thenReturn("value");

        messageConverterReadingService
            .addMessageConverter(converter1)
            .addMessageConverter(converter2);
        
        //When
        messageConverterReadingService.read(in, MediaType.TEXT_XML, Object.class);
        
        //Then
        verify(converter2, never()).canRead(Object.class, MediaType.TEXT_XML);
        verify(converter1).canRead(Object.class, MediaType.TEXT_XML);
    }
    
    @Test
    @SneakyThrows
    void checkThatMessageConverterIsCalledWithUnderlyingInputStream() {
        //Given
        InputStream in = mock(InputStream.class);
        HttpMessageConverter converter = mock(HttpMessageConverter.class);
        
        when(converter.canRead(Object.class, MediaType.TEXT_XML)).thenReturn(Boolean.TRUE);
        messageConverterReadingService.addMessageConverter(converter);
        
        //When
        messageConverterReadingService.read(in, MediaType.TEXT_XML, Object.class);
        
        //Then
        ArgumentCaptor<HttpInputMessage> argument = ArgumentCaptor.forClass(HttpInputMessage.class);
        verify(converter).read(eq(Object.class), argument.capture());
        assertEquals(in, argument.getValue().getBody());
    }
    
    @Test
    @SneakyThrows
    public void checkThatMessageConverterIsCalledWithMediaTypeSet() {
        //Given
        InputStream in = mock(InputStream.class);
        HttpMessageConverter converter = mock(HttpMessageConverter.class);
        
        when(converter.canRead(Object.class, MediaType.TEXT_XML)).thenReturn(Boolean.TRUE);
        messageConverterReadingService.addMessageConverter(converter);
        
        //When
        messageConverterReadingService.read(in, MediaType.TEXT_XML, Object.class);
        
        //Then
        ArgumentCaptor<HttpInputMessage> argument = ArgumentCaptor.forClass(HttpInputMessage.class);
        verify(converter).read(eq(Object.class), argument.capture());
        assertEquals(MediaType.TEXT_XML, argument.getValue().getHeaders().getContentType());
    }
}
