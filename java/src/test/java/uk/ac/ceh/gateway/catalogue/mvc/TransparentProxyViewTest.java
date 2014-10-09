package uk.ac.ceh.gateway.catalogue.mvc;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author cjohn
 */
public class TransparentProxyViewTest {
    
    @Test
    public void checkThatProxyingCopiesData() throws Exception {
        //Given
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(servletResponse.getOutputStream()).thenReturn(outputStream);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        
        HttpEntity entity = mock(HttpEntity.class, RETURNS_DEEP_STUBS);
        when(entity.getContentType().getValue()).thenReturn("content-type");
        
        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(any(HttpGet.class))).thenReturn(response);
        
        //When
        TransparentProxyView proxy = new TransparentProxyView(httpClient, "url");
        proxy.render(null, null, servletResponse);
        
        //Then
        verify(entity).writeTo(outputStream);
        verify(servletResponse).setContentType("content-type");
    }
}
