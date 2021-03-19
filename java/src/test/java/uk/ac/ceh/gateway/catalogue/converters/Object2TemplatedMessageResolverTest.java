package uk.ac.ceh.gateway.catalogue.converters;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class Object2TemplatedMessageResolverTest {
    @Mock Configuration configuration;
    
    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void checkThatCantRead() {
        //Given
        Object2TemplatedMessageConverter<Object> converter = 
                new Object2TemplatedMessageConverter(Object.class, configuration);
        
        //When
        boolean canRead = converter.canRead(Object.class, MediaType.ALL);
        
        //Then
        assertFalse("Expected the writer to not be able to read", canRead);
    }
    
    @Test
    public void checkThatExceptionIsThrownIfReadIsAttempted() throws IOException {
        Assertions.assertThrows(HttpMessageNotReadableException.class, () -> {
            //Given
            Object2TemplatedMessageConverter<Object> converter =
                    new Object2TemplatedMessageConverter(Object.class, configuration);

            HttpInputMessage in = mock(HttpInputMessage.class);

            //When
            converter.readInternal(Object.class, in);

            //Then
            fail("Expected http message not readable exception");
        });
    }
    
    @Test
    public void checkSupportedMediaTypesAreRead() {
        //Given
        @ConvertUsing(
            @Template(called="template",whenRequestedAs="application/json")
        )
        class MyType {}
        
        Object2TemplatedMessageConverter<MyType> converter = 
                new Object2TemplatedMessageConverter(MyType.class, configuration);
        
        //When
        List<MediaType> supportedTypes = converter.getSupportedMediaTypes();
        
        //Then
        assertEquals("Expected one element", 1, supportedTypes.size() );
        assertEquals("Expected mediatype to be json", MediaType.APPLICATION_JSON, supportedTypes.get(0));
    }
    
    @Test
    public void checkTemplateIsCalledForProcessingOnWrite() throws IOException, TemplateException {
        //Given
        @ConvertUsing(
            @Template(called="bob",whenRequestedAs="application/xml")
        )
        class MyType {}
        
        HttpOutputMessage message = mock(HttpOutputMessage.class, RETURNS_DEEP_STUBS);
        OutputStream out = mock(OutputStream.class);
        when(message.getBody()).thenReturn(out);
        when(message.getHeaders().getContentType()).thenReturn(MediaType.APPLICATION_XML);
        
        Object2TemplatedMessageConverter<MyType> converter = 
                new Object2TemplatedMessageConverter(MyType.class, configuration);
        
        freemarker.template.Template freemarkerTemplate = mock(freemarker.template.Template.class);
        when(configuration.getTemplate("bob")).thenReturn(freemarkerTemplate);
        
        MyType dataToProcess = new MyType();
        
        //When
        converter.writeInternal(dataToProcess, message);
        
        //Then
        ArgumentCaptor<String> template = ArgumentCaptor.forClass(String.class);
        verify(configuration).getTemplate(template.capture());
        assertEquals("Expected the template, bob", "bob", template.getValue());
        
        verify(freemarkerTemplate).process(eq(dataToProcess), any(Writer.class));
    }
    
    @Test
    public void checkTemplateExceptionThrowsHttpNotWritableException() throws IOException, TemplateException {
        Assertions.assertThrows(HttpMessageNotWritableException.class, () -> {
            //Given
            @ConvertUsing(
                    @Template(called = "bob", whenRequestedAs = "application/xml")
            )
            class MyType {
            }

            HttpOutputMessage message = mock(HttpOutputMessage.class, RETURNS_DEEP_STUBS);
            OutputStream out = mock(OutputStream.class);
            when(message.getBody()).thenReturn(out);
            when(message.getHeaders().getContentType()).thenReturn(MediaType.APPLICATION_XML);

            Object2TemplatedMessageConverter<MyType> converter =
                    new Object2TemplatedMessageConverter(MyType.class, configuration);

            freemarker.template.Template freemarkerTemplate = mock(freemarker.template.Template.class);
            when(configuration.getTemplate("bob")).thenReturn(freemarkerTemplate);

            MyType dataToProcess = new MyType();

            doThrow(new TemplateException("Epic failure", null))
                    .when(freemarkerTemplate)
                    .process(eq(dataToProcess), any(Writer.class));

            //When
            converter.writeInternal(dataToProcess, message);

            //Then
            fail("Expected to fail with an http not writable exception");
        });
    }
    
    @Test
    public void checkCanWriteAnnotatedType() {
        //Given
        @ConvertUsing(
            @Template(called="bob",whenRequestedAs="application/xml")
        )
        class MyType {}
        
        Object2TemplatedMessageConverter<MyType> converter = 
                new Object2TemplatedMessageConverter(MyType.class, configuration);
        
        //When
        boolean canWrite = converter.canWrite(MyType.class, MediaType.APPLICATION_XML);
        
        //Then
        assertTrue("Expected to be able to write xml from type", canWrite);      
    }
    
    
    @Test
    public void checkCantWriteUndefinedTypeAnnotatedType() {
        //Given
        @ConvertUsing(
            @Template(called="bob",whenRequestedAs="application/xml")
        )
        class MyType {}
        
        Object2TemplatedMessageConverter<MyType> converter = 
                new Object2TemplatedMessageConverter(MyType.class, configuration);
        
        //When
        boolean canWrite = converter.canWrite(MyType.class, MediaType.TEXT_HTML);
        
        //Then
        assertFalse("Didn't expect to be able to write html from type", canWrite);      
    }
    
    @Test
    public void checkSubTypeCanAlsoBeRead() {
        //Given
        @ConvertUsing(@Template(called="bob",whenRequestedAs="application/xml"))
        class MyType {}        
        class MySubType extends MyType {}
        
        Object2TemplatedMessageConverter<MyType> converter = 
                new Object2TemplatedMessageConverter(MyType.class, configuration);
        
        //When
        boolean supports = converter.supports(MySubType.class);
        
        //Then
        assertTrue("Expected to be able to support subtype", supports);
    }
    
    @Test
    public void checkRandomTypeCantBeRead() {
        @ConvertUsing(@Template(called="bob",whenRequestedAs="application/xml"))
        class MyType {}
        
        Object2TemplatedMessageConverter<MyType> converter = 
                new Object2TemplatedMessageConverter(MyType.class, configuration);
        
        //When
        boolean supports = converter.supports(String.class);
        
        //Then
        assertFalse("Didn't expect to be able to support string", supports);
    }
}
