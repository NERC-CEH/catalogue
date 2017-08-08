package uk.ac.ceh.gateway.catalogue.converters;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxy;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxyException;
import uk.ac.ceh.gateway.catalogue.model.UpstreamInvalidMediaTypeException;
/**
 *
 * @author cjohn
 */
public class TransparentProxyMessageConverterTest {
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) CloseableHttpClient httpClient;
    TransparentProxyMessageConverter converter;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
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
        assertFalse("Didn't expect to be able to read", read);
    }
    
    @Test
    public void checkThatCanWrite() {
        //Given
        Class objectClass = TransparentProxy.class;
        MediaType type = MediaType.ALL;
        
        //When
        boolean write = converter.canWrite(objectClass, type);
        
        //Then
        assertTrue("Expected to be able to write the given message type", write);
    }
    
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
        fail("Expecetd to fail with a networking error");
    }
}
