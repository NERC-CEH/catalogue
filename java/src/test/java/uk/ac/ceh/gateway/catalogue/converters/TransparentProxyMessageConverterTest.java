package uk.ac.ceh.gateway.catalogue.converters;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxy;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxyException;
import uk.ac.ceh.gateway.catalogue.model.UpstreamInvalidMediaTypeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TransparentProxyMessageConverterTest {
    @Mock ClientHttpRequestFactory requestFactory;
    @Mock ClientHttpRequest request;
    TransparentProxyMessageConverter converter;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        when(requestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenReturn(request);
        this.converter = spy(new TransparentProxyMessageConverter(requestFactory));
    }

    private ClientHttpResponse givenUpstreamResponse(String contentType) throws IOException {
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_TYPE, contentType);
        InputStream body = mock(InputStream.class);
        when(response.getHeaders()).thenReturn(responseHeaders);
        when(response.getBody()).thenReturn(body);
        when(request.execute()).thenReturn(response);
        return response;
    }

    @Test
    public void checkThatProxyingCopiesData() throws Exception {
        //Given
        HttpOutputMessage message = mock(HttpOutputMessage.class);
        HttpHeaders headers = mock(HttpHeaders.class);

        OutputStream outputStream = mock(OutputStream.class);
        when(message.getBody()).thenReturn(outputStream);
        when(message.getHeaders()).thenReturn(headers);

        ClientHttpResponse response = givenUpstreamResponse("content-type");

        //When
        TransparentProxy proxy = new TransparentProxy("url");
        converter.write(proxy, null, message);

        //Then
        verify(response.getBody()).transferTo(outputStream);
        verify(headers).set(HttpHeaders.CONTENT_TYPE, "content-type");
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
            when(request.getUri()).thenReturn(URI.create("url"));
            when(request.getDesiredMediaType()).thenReturn(MediaType.parseMediaType(requiredMediaType));

            givenUpstreamResponse("incompatible/media");

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
        when(request.getUri()).thenReturn(URI.create("url"));
        when(request.getDesiredMediaType()).thenReturn(MediaType.parseMediaType(requiredMediaType));

        givenUpstreamResponse("image/png");

        HttpOutputMessage message = mock(HttpOutputMessage.class);
        HttpHeaders headers = mock(HttpHeaders.class);

        OutputStream outputStream = mock(OutputStream.class);
        when(message.getBody()).thenReturn(outputStream);
        when(message.getHeaders()).thenReturn(headers);

        //When
        converter.write(request, null, message);

        //Then
        verify(converter).copyAndClose(any(InputStream.class), any(HttpOutputMessage.class));
    }

    @Test
    public void checkThatUpStreamMediaTypeMustBeValidIfRequiringACompatibleMediaType() throws IOException {
        Assertions.assertThrows(UpstreamInvalidMediaTypeException.class, () -> {
            //Given
            String requiredMediaType = "image/*";
            TransparentProxy request = mock(TransparentProxy.class);
            when(request.getUri()).thenReturn(URI.create("url"));
            when(request.getDesiredMediaType()).thenReturn(MediaType.parseMediaType(requiredMediaType));

            givenUpstreamResponse("WMS_TYPE");

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
            when(request.getUri()).thenReturn(URI.create("url"));
            when(this.request.execute()).thenThrow(new IOException("Whoops, no internet"));

            //When
            converter.write(request, null, null);

            //Then
            fail("Expected to fail with a networking error");
        });
    }
}
