package uk.ac.ceh.gateway.catalogue.services;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author cjohn
 */
public class MapServerDetailsServiceTest {
    private MapServerDetailsService service;
    
    @Before
    public void init() {
        service = new MapServerDetailsService("https://catalogue.ceh.ac.uk");
    }
    
    @Test
    public void checkThatRewritesToHostedMapserverRequest() {
        //Given
        String localRequest = "https://catalogue.ceh.ac.uk/documents/ID/wms?REQUEST=WMS";
        
        //When
        String request = service.rewriteToLocalWmsRequest(localRequest);
        
        //Then
        assertEquals(request, "http://mapserver/ID?REQUEST=WMS");
    }
    
    @Test
    public void checkThatOtherServicePassesThrough() {
        //Given
        String externalRequest = "http://somewhere.out.side/as/A/wms?REQUEST=WMS";
        
        //When
        String request = service.rewriteToLocalWmsRequest(externalRequest);
        
        //Then
        assertEquals(request, externalRequest);
    }
}
