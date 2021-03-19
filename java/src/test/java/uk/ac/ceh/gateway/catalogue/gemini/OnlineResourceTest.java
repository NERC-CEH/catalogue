package uk.ac.ceh.gateway.catalogue.gemini;

import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.Test;

public class OnlineResourceTest {
    @Test
    public void checkThatWMSURLReturnsGetCapabilitiesType() {
        //Given
        OnlineResource resource = OnlineResource.builder().url("http://wms.com?REQUEST=GetCapabilities&SERVICE=WMS&").build();
        
        //When
        OnlineResource.Type type = resource.getType();
        
        //Then
        assertEquals("Expected to find get capabilites type", OnlineResource.Type.WMS_GET_CAPABILITIES, type);
    }
    
    @Test
    public void checkThatCaseForGetCapabilitesDoesntMatter() {
        //Given
        OnlineResource resource = OnlineResource.builder().url("http://wms.com?request=getcapabilities&SERVICE=WMS").build();
        
        //When
        OnlineResource.Type type = resource.getType();
        
        //Then
        assertEquals("Expected to find get capabilites type", OnlineResource.Type.WMS_GET_CAPABILITIES, type);
    }
    
    @Test
    public void checkThatURLWithGetCapabilitesInsideItIsNotFlagged() {
        //Given
        OnlineResource resource = OnlineResource.builder().url("http://www.google.com/getcapabilities/somethingelse").build();
        
        //When
        OnlineResource.Type type = resource.getType();
        
        //Then
        assertEquals("Expected to not find get capabilites type", OnlineResource.Type.OTHER, type);
    }
    
    @Test
    public void checkThatURLWithParameterAfterAmpersandMatchesGetCapabilites() {
        //Given
        OnlineResource resource = OnlineResource.builder().url("http://wms.com?SERVICE=WMS&REQUEST=GetCapabilities&").build();
        
        //When
        OnlineResource.Type type = resource.getType();
        
        //Then
        assertEquals("Expected to find get capabilites type", OnlineResource.Type.WMS_GET_CAPABILITIES, type);
    }
    
}
