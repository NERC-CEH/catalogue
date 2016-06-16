package uk.ac.ceh.gateway.catalogue.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.ExternalResourceFailureException;
import uk.ac.ceh.gateway.catalogue.model.NotAGetCapabilitiesResourceException;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;

/**
 *
 * @author cjohn
 */
public class GetCapabilitiesObtainerServiceTest {
    @Mock RestTemplate rest;
    @Mock MapServerDetailsService mapServerDetailsService;
    
    private GetCapabilitiesObtainerService service;
    
    @Before
    public void createOnlineController() {
        MockitoAnnotations.initMocks(this);
        
        service = spy(new GetCapabilitiesObtainerService(rest, mapServerDetailsService));
    }
    
    @Test(expected=NotAGetCapabilitiesResourceException.class)
    public void checkThatGetWMSCapabilitiesDoesntWorkWithIncorrectOnlineResource() {
        //Given
        OnlineResource resource = OnlineResource.builder().url("http://not.a.wms").build();
        
        //When
        service.getWmsCapabilities(resource);
        
        //Then
        fail("Expected to fail with invalid capabilites exception");
    }
    
    @Test
    public void checkThatRestTemplateIsCalledWithValidWMSCapabilitesType() {
        //Given
        OnlineResource resource = OnlineResource.builder().url("https://www.google.com/wms?REQUEST=GetCapabilities&SERVICE=WMS").build();
        
        WmsCapabilities wmsCaps = mock(WmsCapabilities.class);
        when(mapServerDetailsService.rewriteToLocalWmsRequest(resource.getUrl())).thenReturn("https://rewritten");
        when(rest.getForObject(eq("https://rewritten"), eq(WmsCapabilities.class))).thenReturn(wmsCaps);
        
        //When
        WmsCapabilities result = service.getWmsCapabilities(resource);
        
        //Then
        assertThat("Expected my wms mock to come out", result, equalTo(wmsCaps));
    }
    
    @Test(expected=ExternalResourceFailureException.class)
    public void checkExcpetionReturnedWhenRestClientFails() {
        //Given
        OnlineResource resource = OnlineResource.builder().url("https://www.google.com/wms?REQUEST=GetCapabilities&SERVICE=WMS").build();
        
        when(rest.getForObject(any(String.class), eq(WmsCapabilities.class)))
                .thenThrow(new RestClientException("failed"));
        
        //When
        service.getWmsCapabilities(resource);
        
        //Then
        fail("Expected to fail with exception");
    }
    
    @Test(expected=ExternalResourceFailureException.class)
    public void checkExcpetionReturnedWhenAnIllegalArgumentExceptionOccurs() {
        //Given
        OnlineResource resource = OnlineResource.builder().url("https://www.google.com/wms?REQUEST=GetCapabilities&SERVICE=WMS").build();
        
        when(rest.getForObject(any(String.class), eq(WmsCapabilities.class)))
                .thenThrow(new IllegalArgumentException("failed"));
        
        //When
        service.getWmsCapabilities(resource);
        
        //Then
        fail("Expected to fail with exception");
    }
}
