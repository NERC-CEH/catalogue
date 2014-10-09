package uk.ac.ceh.gateway.catalogue.services;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.ogc.Layer;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;

/**
 *
 * @author cjohn
 */
public class MapProxyServiceTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();
    @Mock GetCapabilitiesObtainerService getCapabilitiesService;
    
    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void checkThatOnlineResourceCanCreateAMapProxyConfigFile() throws IOException, MapProxyServiceException {
        //Given
        File proxyFiles = folder.newFolder();
        MapProxyService service = new MapProxyService(proxyFiles, getCapabilitiesService);
        
        Layer layer = mock(Layer.class);
        when(layer.getName()).thenReturn("LayerName");
        when(layer.getTitle()).thenReturn("Layer title");
        
        WmsCapabilities wmsCapabilities = mock(WmsCapabilities.class);
        when(wmsCapabilities.getDirectMap()).thenReturn("directmap");
        when(wmsCapabilities.getLayers()).thenReturn(Arrays.asList(layer));
                
        OnlineResource resource = new OnlineResource("http://wms.service", "named", "with this description");
        when(getCapabilitiesService.getWmsCapabilities(resource)).thenReturn(wmsCapabilities);
        
        //When
        String tiledMapService = service.getTiledMapService(resource);
        
        //Then
        assertTrue("Expected a map proxy file to be created", new File(proxyFiles, tiledMapService + ".yaml").exists());
    }
}
