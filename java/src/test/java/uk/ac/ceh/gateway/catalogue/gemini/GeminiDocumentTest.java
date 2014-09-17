package uk.ac.ceh.gateway.catalogue.gemini;

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author cjohn
 */
public class GeminiDocumentTest {
    @Test
    public void checkIfIsMapViewableIfGetCapabilitiesOnlineResourceExists() {
        //Given
        OnlineResource wmsResource = new OnlineResource(
                "http://www.com?request=GetCapabilities", 
                "wms resource", 
                "wms description");
        GeminiDocument document = new GeminiDocument();
        document.setOnlineResources(Arrays.asList(wmsResource));
        
        //When
        boolean isMapViewable = document.isMapViewable();
        
        //Then
        assertTrue("Expected to be map viewable", isMapViewable);
    }
    
    
    @Test
    public void checkIfIsntMapViewableIfGetCapabilitiesOnlineResourceDoesntExists() {
        //Given
        OnlineResource wmsResource = new OnlineResource(
                "http://www.google.com", 
                "wms resource", 
                "wms description");
        GeminiDocument document = new GeminiDocument();
        document.setOnlineResources(Arrays.asList(wmsResource));
        
        //When
        boolean isMapViewable = document.isMapViewable();
        
        //Then
        assertFalse("Expected to not be map viewable", isMapViewable);
    }
    
    @Test
    public void getLinkToMapViewer() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class, CALLS_REAL_METHODS);
        doReturn(true).when(document).isMapViewable();
        doReturn("metadataId").when(document).getId();
        
        //When
        String url = document.getMapViewerUrl();
        
        //Then
        assertEquals("Expected a map viewer url", "/maps#layers/metadataId", url);
    }
    
    @Test
    public void checkThatMapViewerURLIsNullIfNotMapViewable() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.isMapViewable()).thenReturn(false);
        
        //When
        String url = document.getMapViewerUrl();
        
        //Then
        assertNull("Expected to get a null url for the map viewer", url);
    }
}
