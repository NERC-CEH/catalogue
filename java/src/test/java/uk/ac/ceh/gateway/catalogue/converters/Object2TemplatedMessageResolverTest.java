package uk.ac.ceh.gateway.catalogue.converters;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Object2TemplatedMessageResolverTest {
    @Mock Configuration configuration;

    @Test
    void checkThatCantRead() {
        //Given
        val converter = new Object2TemplatedMessageConverter<>(Object.class, configuration);

        //When
        boolean canRead = converter.canRead(Object.class, MediaType.ALL);

        //Then
        assertFalse(canRead);
    }

    @Test
    void checkThatExceptionIsThrownIfReadIsAttempted() {
        //Given
        val converter = new Object2TemplatedMessageConverter<>(Object.class, configuration);
        val in = mock(HttpInputMessage.class);

        //When
        Assertions.assertThrows(HttpMessageNotReadableException.class, () ->
            converter.readInternal(Object.class, in)
        );
    }

    @Test
    void checkSupportedMediaTypesAreRead() {
        //Given
        @ConvertUsing(
            @Template(called="template",whenRequestedAs="application/json")
        )
        class MyType {}

        val converter = new Object2TemplatedMessageConverter<MyType>(MyType.class, configuration);

        //When
        List<MediaType> supportedTypes = converter.getSupportedMediaTypes();

        //Then
        assertEquals(1, supportedTypes.size() );
        assertEquals(MediaType.APPLICATION_JSON, supportedTypes.get(0));
    }

    @Test
    void checkTemplateIsCalledForProcessingOnWrite() throws IOException, TemplateException {
        //Given
        @ConvertUsing(
            @Template(called="bob",whenRequestedAs="application/xml")
        )
        class MyType {}

        val message = mock(HttpOutputMessage.class, RETURNS_DEEP_STUBS);
        val out = mock(OutputStream.class);
        given(message.getBody()).willReturn(out);
        given(message.getHeaders().getContentType()).willReturn(MediaType.APPLICATION_XML);

        val converter = new Object2TemplatedMessageConverter<MyType>(MyType.class, configuration);

        freemarker.template.Template freemarkerTemplate = mock(freemarker.template.Template.class);
        given(configuration.getTemplate("bob")).willReturn(freemarkerTemplate);

        MyType dataToProcess = new MyType();

        //When
        converter.writeInternal(dataToProcess, message);

        //Then
        verify(freemarkerTemplate).process(eq(dataToProcess), any(Writer.class));
    }

    @Test
    @SneakyThrows
    void checkTemplateExceptionThrowsHttpNotWritableException() {
        //Given
        @ConvertUsing(
                @Template(called = "bob", whenRequestedAs = "application/xml")
        )
        class MyType {
        }

        val message = mock(HttpOutputMessage.class, RETURNS_DEEP_STUBS);
        val out = mock(OutputStream.class);
        given(message.getBody()).willReturn(out);
        given(message.getHeaders().getContentType()).willReturn(MediaType.APPLICATION_XML);

        val converter = new Object2TemplatedMessageConverter<MyType>(MyType.class, configuration);

        freemarker.template.Template freemarkerTemplate = mock(freemarker.template.Template.class);
        given(configuration.getTemplate("bob")).willReturn(freemarkerTemplate);

        MyType dataToProcess = new MyType();

        doThrow(new TemplateException("Epic failure", null))
                .when(freemarkerTemplate)
                .process(eq(dataToProcess), any(Writer.class));

        //When
        Assertions.assertThrows(HttpMessageNotWritableException.class, () ->
            converter.writeInternal(dataToProcess, message)
        );
    }

    @Test
    void checkCanWriteAnnotatedType() {
        //Given
        @ConvertUsing(
            @Template(called="bob",whenRequestedAs="application/xml")
        )
        class MyType {}

       val converter = new Object2TemplatedMessageConverter<MyType>(MyType.class, configuration);

        //When
        boolean canWrite = converter.canWrite(MyType.class, MediaType.APPLICATION_XML);

        //Then
        assertTrue(canWrite);
    }


    @Test
    void checkCantWriteUndefinedTypeAnnotatedType() {
        //Given
        @ConvertUsing(
            @Template(called="bob",whenRequestedAs="application/xml")
        )
        class MyType {}

        val converter = new Object2TemplatedMessageConverter<MyType>(MyType.class, configuration);

        //When
        boolean canWrite = converter.canWrite(MyType.class, MediaType.TEXT_HTML);

        //Then
        assertFalse(canWrite);
    }

    @Test
    void checkSubTypeCanAlsoBeRead() {
        //Given
        @ConvertUsing(@Template(called="bob",whenRequestedAs="application/xml"))
        class MyType {}
        class MySubType extends MyType {}

        val converter = new Object2TemplatedMessageConverter<MyType>(MyType.class, configuration);

        //When
        boolean supports = converter.supports(MySubType.class);

        //Then
        assertTrue(supports);
    }

    @Test
    public void checkRandomTypeCantBeRead() {
        @ConvertUsing(@Template(called="bob",whenRequestedAs="application/xml"))
        class MyType {}

        val converter = new Object2TemplatedMessageConverter<MyType>(MyType.class, configuration);

        //When
        boolean supports = converter.supports(String.class);

        //Then
        assertFalse(supports);
    }
}
