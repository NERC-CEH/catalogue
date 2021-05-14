package uk.ac.ceh.gateway.catalogue.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.ExternalResourceFailureException;
import uk.ac.ceh.gateway.catalogue.model.NotAGetCapabilitiesResourceException;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetCapabilitiesObtainerServiceTest {
    @Mock RestTemplate rest;
    @Mock MapServerDetailsService mapServerDetailsService;
    
    private GetCapabilitiesObtainerService service;
    
    @BeforeEach
    public void createOnlineController() {
        service = spy(new GetCapabilitiesObtainerService(rest, mapServerDetailsService));
    }
    
    @Test
    public void checkThatGetWMSCapabilitiesDoesntWorkWithIncorrectOnlineResource() throws URISyntaxException {
        Assertions.assertThrows(NotAGetCapabilitiesResourceException.class, () -> {
            //Given
            OnlineResource resource = OnlineResource.builder().url("http://not.a.wms").build();

            //When
            service.getWmsCapabilities(resource);

            //Then
            fail("Expected to fail with invalid capabilites exception");
        });
    }
    
    @Test
    public void checkThatRestTemplateIsCalledWithValidWMSCapabilitesType() throws URISyntaxException {
        //Given
        OnlineResource resource = OnlineResource.builder().url("https://www.google.com/wms?REQUEST=GetCapabilities&SERVICE=WMS").build();
        
        WmsCapabilities wmsCaps = mock(WmsCapabilities.class);
        when(mapServerDetailsService.rewriteToLocalWmsRequest(resource.getUrl())).thenReturn("https://rewritten");
        when(rest.getForObject(eq(new URI("https://rewritten")), eq(WmsCapabilities.class))).thenReturn(wmsCaps);
        
        //When
        WmsCapabilities result = service.getWmsCapabilities(resource);
        
        //Then
        assertThat("Expected my wms mock to come out", result, equalTo(wmsCaps));
    }
    
    @Test
    public void checkExcpetionReturnedWhenRestClientFails() {
        Assertions.assertThrows(ExternalResourceFailureException.class, () -> {
        //Given
        OnlineResource resource = OnlineResource.builder().url("https://www.google.com/wms?REQUEST=GetCapabilities&SERVICE=WMS").build();
        
        when(mapServerDetailsService.rewriteToLocalWmsRequest(any(String.class))).thenReturn("Anything");
        when(rest.getForObject(any(URI.class), eq(WmsCapabilities.class)))
                .thenThrow(new RestClientException("failed"));
        
        //When
        service.getWmsCapabilities(resource);
        
        //Then
        fail("Expected to fail with exception");
        });
    }
    
    @Test
    public void checkExcpetionReturnedWhenAnIllegalArgumentExceptionOccurs() throws URISyntaxException {
        Assertions.assertThrows(ExternalResourceFailureException.class, () -> {
            //Given
            OnlineResource resource = OnlineResource.builder().url("https://www.google.com/wms?REQUEST=GetCapabilities&SERVICE=WMS").build();

            when(mapServerDetailsService.rewriteToLocalWmsRequest(any(String.class))).thenReturn("Anything");
            when(rest.getForObject(any(URI.class), eq(WmsCapabilities.class)))
                    .thenThrow(new IllegalArgumentException("failed"));

            //When
            service.getWmsCapabilities(resource);

            //Then
            fail("Expected to fail with exception");
        });
    }
}
