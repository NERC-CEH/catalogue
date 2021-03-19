package uk.ac.ceh.gateway.catalogue.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class MessageConverterReadingServiceTest {
    @Spy List<HttpMessageConverter<?>> httpConverters;
    
    MessageConverterReadingService messageConverter;
    
    @BeforeEach
    public void createMessageConverter() {
        httpConverters = new ArrayList<>();
        MockitoAnnotations.initMocks(this);
        messageConverter = new MessageConverterReadingService(httpConverters);
    }
    
    @Test
    public void checkThatCanAddHttpConverter() {
        //Given
        HttpMessageConverter converter = mock(HttpMessageConverter.class);
        
        //When
        messageConverter.addMessageConverter(converter);
        
        //Then
        verify(httpConverters).add(converter);
    }
    
    @Test
    public void checkThatUnknownContentTypeExceptionIsThrownIfMessageConverterIsNotPresent() throws IOException, UnknownContentTypeException {
        Assertions.assertThrows(UnknownContentTypeException.class, () -> {
            //Given
            InputStream in = mock(InputStream.class);

            //When
            messageConverter.read(in, MediaType.TEXT_HTML, Object.class);

            //Then
            fail("Didn't expect to be able to read HTML");
        });
    }
    
    @Test
    public void checkThatFirstMatchingMessageConverterReturns() throws IOException, UnknownContentTypeException {
        //Given
        InputStream in = mock(InputStream.class);
        HttpMessageConverter converter1 = mock(HttpMessageConverter.class);
        HttpMessageConverter converter2 = mock(HttpMessageConverter.class);
        
        when(converter1.canRead(Object.class, MediaType.TEXT_XML)).thenReturn(Boolean.TRUE);
        when(converter1.read(eq(Object.class), any(HttpInputMessage.class))).thenReturn("value");
        messageConverter.addMessageConverter(converter1)
                        .addMessageConverter(converter2);
        
        //When
        messageConverter.read(in, MediaType.TEXT_XML, Object.class);
        
        //Then
        verify(converter2, never()).canRead(Object.class, MediaType.TEXT_XML);
        verify(converter1).canRead(Object.class, MediaType.TEXT_XML);
    }
    
    @Test
    public void checkThatMessageConverterIsCalledWithUnderlyingInputStream() throws IOException, UnknownContentTypeException {
        //Given
        InputStream in = mock(InputStream.class);
        HttpMessageConverter converter = mock(HttpMessageConverter.class);
        
        when(converter.canRead(Object.class, MediaType.TEXT_XML)).thenReturn(Boolean.TRUE);
        messageConverter.addMessageConverter(converter);
        
        //When
        messageConverter.read(in, MediaType.TEXT_XML, Object.class);
        
        //Then
        ArgumentCaptor<HttpInputMessage> argument = ArgumentCaptor.forClass(HttpInputMessage.class);
        verify(converter).read(eq(Object.class), argument.capture());
        assertEquals("Expected the input stream to be passed to the http message converter", in, argument.getValue().getBody());
    }
    
    @Test
    public void checkThatMessageConverterIsCalledWithMediaTypeSet() throws IOException, UnknownContentTypeException {
        //Given
        InputStream in = mock(InputStream.class);
        HttpMessageConverter converter = mock(HttpMessageConverter.class);
        
        when(converter.canRead(Object.class, MediaType.TEXT_XML)).thenReturn(Boolean.TRUE);
        messageConverter.addMessageConverter(converter);
        
        //When
        messageConverter.read(in, MediaType.TEXT_XML, Object.class);
        
        //Then
        ArgumentCaptor<HttpInputMessage> argument = ArgumentCaptor.forClass(HttpInputMessage.class);
        verify(converter).read(eq(Object.class), argument.capture());
        assertEquals(
            "Expected the media type to be passed to the underlying http message converter", 
            MediaType.TEXT_XML, 
            argument.getValue().getHeaders().getContentType()
        );
    }
}
