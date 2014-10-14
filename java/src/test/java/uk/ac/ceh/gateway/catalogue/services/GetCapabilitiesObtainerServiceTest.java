package uk.ac.ceh.gateway.catalogue.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.controller;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.NotAGetCapabilitiesResourceException;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;

/**
 *
 * @author cjohn
 */
public class GetCapabilitiesObtainerServiceTest {
    @Mock RestTemplate rest;
    
    private GetCapabilitiesObtainerService service;
    
    @Before
    public void createOnlineController() {
        MockitoAnnotations.initMocks(this);
        
        service = spy(new GetCapabilitiesObtainerService(rest));
    }
    
    @Test(expected=NotAGetCapabilitiesResourceException.class)
    public void checkThatGetWMSCapabilitiesDoesntWorkWithIncorrectOnlineResource() {
        //Given
        OnlineResource resource = new OnlineResource(
                "http://not.a.wms", "nothing", "nothing");
        
        //When
        service.getWmsCapabilities(resource);
        
        //Then
        fail("Expected to fail with invalid capabilites exception");
    }
    
    @Test
    public void checkThatRestTemplateIsCalledWithValidWMSCapabilitesType() {
        //Given
        OnlineResource resource = new OnlineResource(
                "https://www.google.com/wms?REQUEST=GetCapabilities", "test wms", "test wms");
        
        WmsCapabilities wmsCaps = mock(WmsCapabilities.class);
        
        when(rest.getForObject(eq("https://www.google.com/wms?REQUEST=GetCapabilities"), eq(WmsCapabilities.class))).thenReturn(wmsCaps);
        
        //When
        WmsCapabilities result = service.getWmsCapabilities(resource);
        
        //Then
        assertThat("Expected my wms mock to come out", result, equalTo(wmsCaps));
    }
}