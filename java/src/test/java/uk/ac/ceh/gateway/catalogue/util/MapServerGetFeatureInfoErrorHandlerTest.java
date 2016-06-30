package uk.ac.ceh.gateway.catalogue.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import static org.eclipse.jgit.util.HttpSupport.response;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.MAPSERVER_GML_VALUE;
import uk.ac.ceh.gateway.catalogue.model.MapServerException;

/**
 *
 * @author cjohn
 */
public class MapServerGetFeatureInfoErrorHandlerTest {
    private MapServerGetFeatureInfoErrorHandler handler;
    
    @Before
    public void init() {
        handler = new MapServerGetFeatureInfoErrorHandler();
    }
    
    @Test
    public void checkThatGMLMediaTypeIsFine() throws IOException {
        //Given
        ClientHttpResponse response = mock(ClientHttpResponse.class, RETURNS_DEEP_STUBS);
        when(response.getHeaders().getContentType()).thenReturn(MediaType.parseMediaType(MAPSERVER_GML_VALUE));
        
        //When
        boolean isError = handler.hasError(response);
        
        //Then
        assertFalse(isError);
    }
    
    
    @Test
    public void checkThatXMLMediaTypeIsFine() throws IOException {
        //Given
        ClientHttpResponse response = mock(ClientHttpResponse.class, RETURNS_DEEP_STUBS);
        when(response.getHeaders().getContentType()).thenReturn(MediaType.TEXT_XML);
        
        //When
        boolean isError = handler.hasError(response);
        
        //Then
        assertTrue(isError);
    }
    
    @Test(expected=MapServerException.class)
    public void checkThatErrorThrowsException() throws IOException {
        //Given
        ClientHttpResponse response = mock(ClientHttpResponse.class, RETURNS_DEEP_STUBS);
        ByteArrayInputStream inputStream = new ByteArrayInputStream("Danger!".getBytes());
        doReturn(inputStream).when(response).getBody();
    
        //When
        handler.handleError(response);
        
        //Then
        fail("Expected an exception");
    }
}
