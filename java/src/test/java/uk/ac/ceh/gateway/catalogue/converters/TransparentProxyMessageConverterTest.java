package uk.ac.ceh.gateway.catalogue.converters;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
<<<<<<< HEAD
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
=======
import org.junit.Before;
import org.junit.Test;
>>>>>>> Convert to SpringBoot
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

<<<<<<< HEAD
import static org.junit.jupiter.api.Assertions.*;
=======
import static org.junit.Assert.*;
>>>>>>> Convert to SpringBoot
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransparentProxyMessageConverterTest {
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) CloseableHttpClient httpClient;
    TransparentProxyMessageConverter converter;
<<<<<<< HEAD
    
    @BeforeEach
=======

    @Before
>>>>>>> Convert to SpringBoot
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
        when(entity.getContentType().getValue()).thenReturn("content-type");

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
<<<<<<< HEAD
    
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

            when(httpClient.execute(any(HttpGet.class)).getEntity().getContentType().getValue())
                    .thenReturn("incompatible/media");

            //When
            converter.write(request, null, null);

            //Then
            fail("Expected incompatible media types to throw exception");
        });
=======

    @Test(expected=HttpMessageNotReadableException.class)
    public void readingResultsInException() throws IOException {
        //Given
        Class objectClass = TransparentProxy.class;
        HttpInputMessage message = mock(HttpInputMessage.class);

        //When
        TransparentProxy read = converter.read(objectClass, message);

        //Then
        fail("Expected to fail with exception");
    }

    @Test(expected=UpstreamInvalidMediaTypeException.class)
    public void checkThatInvalidUpstreamMediaTypeIsNotProxied() throws IOException {
        //Given
        String requiredMediaType = "image/*";
        TransparentProxy request = mock(TransparentProxy.class);
        when(request.getDesiredMediaType()).thenReturn(MediaType.parseMediaType(requiredMediaType));

        when(httpClient.execute(any(HttpGet.class)).getEntity().getContentType().getValue())
                .thenReturn("incompatible/media");

        //When
        converter.write(request, null, null);

        //Then
        fail("Expected incompatible media types to throw exception");
>>>>>>> Convert to SpringBoot
    }


    @Test
    public void checkThatCompatibleMediaTypeIsProxied() throws IOException {
        //Given
        String requiredMediaType = "image/*";
        TransparentProxy request = mock(TransparentProxy.class);
        when(request.getDesiredMediaType()).thenReturn(MediaType.parseMediaType(requiredMediaType));

        when(httpClient.execute(any(HttpGet.class)).getEntity().getContentType().getValue())
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
<<<<<<< HEAD
    
    @Test
    public void checkThatUpStreamMediaTypeMustBeValidIfRequiringACompatibleMediaType() throws IOException {
        Assertions.assertThrows(UpstreamInvalidMediaTypeException.class, () -> {
            //Given
            String requiredMediaType = "image/*";
            TransparentProxy request = mock(TransparentProxy.class);
            when(request.getDesiredMediaType()).thenReturn(MediaType.parseMediaType(requiredMediaType));

            when(httpClient.execute(any(HttpGet.class)).getEntity().getContentType().getValue())
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
            fail("Expecetd to fail with a networking error");
        });
=======

    @Test(expected=UpstreamInvalidMediaTypeException.class)
    public void checkThatUpStreamMediaTypeMustBeValidIfRequiringACompatibleMediaType() throws IOException {
        //Given
        String requiredMediaType = "image/*";
        TransparentProxy request = mock(TransparentProxy.class);
        when(request.getDesiredMediaType()).thenReturn(MediaType.parseMediaType(requiredMediaType));

        when(httpClient.execute(any(HttpGet.class)).getEntity().getContentType().getValue())
                .thenReturn("WMS_TYPE");

        //When
        converter.write(request, null, null);

        //Then
        fail("Expected to fail with an exception");
    }

    @Test(expected=TransparentProxyException.class)
    public void checkThatNetworkIssueResultsInAnException() throws IOException {
        //Given
        TransparentProxy request = mock(TransparentProxy.class);
        when(httpClient.execute(any(HttpGet.class))).thenThrow(new IOException("Whoops, no internet"));

        //When
        converter.write(request, null, null);

        //Then
        fail("Expected to fail with a networking error");
>>>>>>> Convert to SpringBoot
    }
}
