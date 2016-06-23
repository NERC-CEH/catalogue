package uk.ac.ceh.gateway.catalogue.services;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

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
    public void checkThatGeminiDocumentWithServiceDefinitionIsHostable() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        MapDataDefinition definition = mock(MapDataDefinition.class);
        when(document.getMapDataDefinition()).thenReturn(definition);
        
        //When
        boolean isHostable = service.isMapServiceHostable(document);
        
        //Then
        assertThat(isHostable, is(true));   
    }
        
    @Test
    public void checkThatUnknownMetadataDocumentIsNotHostable() {
        //Given
        MetadataDocument document = mock(MetadataDocument.class);
        
        //When
        boolean isHostable = service.isMapServiceHostable(document);
        
        //Then
        assertThat(isHostable, is(false));   
    }
    
    @Test
    public void checkThatRewritesToHostedMapserverRequest() {
        //Given
        String localRequest = "https://catalogue.ceh.ac.uk/maps/ID?REQUEST=WMS";
        
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
    
    @Test
    public void checkThatCanBuildAMapServerRequestUrl() {
        //Given
        String file = "myfileid";
        String queryString = "query";
        
        //When
        String request = service.getLocalWMSRequest(file, queryString);
        
        //Then
        assertEquals(request, "http://mapserver/myfileid?query");
    }
}
