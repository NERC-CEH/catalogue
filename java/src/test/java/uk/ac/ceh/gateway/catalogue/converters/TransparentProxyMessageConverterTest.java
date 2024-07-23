package uk.ac.ceh.gateway.catalogue.converters;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxy;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxyException;
import uk.ac.ceh.gateway.catalogue.model.UpstreamInvalidMediaTypeException;

import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransparentProxyMessageConverterTest {
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) CloseableHttpClient httpClient;
    TransparentProxyMessageConverter converter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.converter = spy(new TransparentProxyMessageConverter(httpClient));
    }

    @Test
    public void checkThatProxyingCopiesData() throws Exception {
        //Given
        HttpOutputMessage message = mock(HttpOutputMessage.class);
        HttpHeaders headers = mock(HttpHeaders.class);

        OutputStream outputStream = mock(OutputStream.class);
        when(message.getBody()).thenReturn(outputStream);
        when(message.getHeaders()).thenReturn(headers);

        CloseableHttpResponse response = mock(CloseableHttpResponse.class);

        HttpEntity entity = mock(HttpEntity.class, RETURNS_DEEP_STUBS);
        when(entity.getContentType()).thenReturn("content-type");

        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(any(HttpGet.class))).thenReturn(response);

        //When
        TransparentProxy proxy = new TransparentProxy("url");
        converter.write(proxy, null, message);

        //Then
        verify(entity).writeTo(outputStream);
        verify(headers).set("Content-Type", "content-type");
    }

    @Test
    public void checkThatCanNotRead() {
        //Given
        Class objectClazz = Object.class;
        MediaType type = null;

        //When
        boolean read = converter.canRead(objectClazz, type);

        //Then
        assertFalse(read);
    }

    @Test
    public void checkThatCanWrite() {
        //Given
        Class objectClass = TransparentProxy.class;
        MediaType type = MediaType.ALL;

        //When
        boolean write = converter.canWrite(objectClass, type);

        //Then
        assertTrue(write);
    }

    @Test
    public void readingResultsInException() throws IOException {
        Assertions.assertThrows(HttpMessageNotReadableException.class, () -> {
            //Given
            Class objectClass = TransparentProxy.class;
            HttpInputMessage message = mock(HttpInputMessage.class);

            //When
            TransparentProxy read = converter.read(objectClass, message);

            //Then
            fail("Expected to fail with exception");
        });
    }

    @Test
    public void checkThatInvalidUpstreamMediaTypeIsNotProxied() throws IOException {
        Assertions.assertThrows(UpstreamInvalidMediaTypeException.class, () -> {
            //Given
            String requiredMediaType = "image/*";
            TransparentProxy request = mock(TransparentProxy.class);
            when(request.getDesiredMediaType()).thenReturn(MediaType.parseMediaType(requiredMediaType));
            when(httpClient.execute(any(HttpGet.class)).getEntity().getContentType())
                    .thenReturn("incompatible/media");
            //When
            converter.write(request, null, null);
            //Then
            fail("Expected incompatible media types to throw exception");
        });
    }


    @Test
    public void checkThatCompatibleMediaTypeIsProxied() throws IOException {
        //Given
        String requiredMediaType = "image/*";
        TransparentProxy request = mock(TransparentProxy.class);
        when(request.getDesiredMediaType()).thenReturn(MediaType.parseMediaType(requiredMediaType));

        when(httpClient.execute(any(HttpGet.class)).getEntity().getContentType())
                .thenReturn("image/png");

        HttpOutputMessage message = mock(HttpOutputMessage.class);
        HttpHeaders headers = mock(HttpHeaders.class);

        OutputStream outputStream = mock(OutputStream.class);
        when(message.getBody()).thenReturn(outputStream);
        when(message.getHeaders()).thenReturn(headers);

        //When
        converter.write(request, null, message);

        //Then
        verify(converter).copyAndClose(any(HttpEntity.class), any(HttpOutputMessage.class));
    }

    @Test
    public void checkThatUpStreamMediaTypeMustBeValidIfRequiringACompatibleMediaType() throws IOException {
        Assertions.assertThrows(UpstreamInvalidMediaTypeException.class, () -> {
            //Given
            String requiredMediaType = "image/*";
            TransparentProxy request = mock(TransparentProxy.class);
            when(request.getDesiredMediaType()).thenReturn(MediaType.parseMediaType(requiredMediaType));

            when(httpClient.execute(any(HttpGet.class)).getEntity().getContentType())
                    .thenReturn("WMS_TYPE");

            //When
            converter.write(request, null, null);

            //Then
            fail("Expected to fail with an exception");
        });
    }

    @Test
    public void checkThatNetworkIssueResultsInAnException() throws IOException {
        Assertions.assertThrows(TransparentProxyException.class, () -> {
            //Given
            TransparentProxy request = mock(TransparentProxy.class);
            when(httpClient.execute(any(HttpGet.class))).thenThrow(new IOException("Whoops, no internet"));

            //When
            converter.write(request, null, null);

            //Then
            fail("Expected to fail with a networking error");
        });
    }
}
