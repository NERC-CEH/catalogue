package uk.ac.ceh.gateway.catalogue.gemini.elements;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author cjohn
 */
public class OnlineResourceTest {
    @Test
    public void checkThatWMSURLReturnsGetCapabilitiesType() {
        //Given
        OnlineResource resource = new OnlineResource("http://wms.com?REQUEST=GetCapabilities&SERVICE=WMS&","","");
        
        //When
        OnlineResource.Type type = resource.getType();
        
        //Then
        assertEquals("Expected to find get capabilites type", OnlineResource.Type.GET_CAPABILITIES, type);
    }
    
    @Test
    public void checkThatCaseForGetCapabilitesDoesntMatter() {
        //Given
        OnlineResource resource = new OnlineResource("http://wms.com?request=getcapabilities","","");
        
        //When
        OnlineResource.Type type = resource.getType();
        
        //Then
        assertEquals("Expected to find get capabilites type", OnlineResource.Type.GET_CAPABILITIES, type);
    }
    
    @Test
    public void checkThatURLWithGetCapabilitesInsideItIsNotFlagged() {
        //Given
        OnlineResource resource = new OnlineResource("http://www.google.com/getcapabilities/somethingelse","","");
        
        //When
        OnlineResource.Type type = resource.getType();
        
        //Then
        assertEquals("Expected to not find get capabilites type", OnlineResource.Type.OTHER, type);
    }
    
    @Test
    public void checkThatURLWithParameterAfterAmpersandMatchesGetCapabilites() {
        //Given
        OnlineResource resource = new OnlineResource("http://wms.com?SERVICE=WMS&REQUEST=GetCapabilities&","","");
        
        //When
        OnlineResource.Type type = resource.getType();
        
        //Then
        assertEquals("Expected to find get capabilites type", OnlineResource.Type.GET_CAPABILITIES, type);
    }
    
}
