package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.http.impl.client.CloseableHttpClient;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.LegendGraphicMissingException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.NoSuchOnlineResourceException;
import uk.ac.ceh.gateway.catalogue.mvc.TransparentProxyView;
import uk.ac.ceh.gateway.catalogue.ogc.Layer;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.GetCapabilitiesObtainerService;
import uk.ac.ceh.gateway.catalogue.services.TMSToWMSGetMapService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
public class OnlineResourceControllerTest {
    @Mock DataRepository<CatalogueUser> repo;
    @Mock CloseableHttpClient httpClient;
    @Mock BundledReaderService<MetadataDocument> documentBundleReader;
    @Mock GetCapabilitiesObtainerService getCapabilitiesObtainerService;
    @Mock TMSToWMSGetMapService tmsToWMSGetMapService;
    
    private OnlineResourceController controller;
    
    @Before
    public void createOnlineController() {
        MockitoAnnotations.initMocks(this);
        
        controller = spy(new OnlineResourceController(repo, httpClient, documentBundleReader, getCapabilitiesObtainerService, tmsToWMSGetMapService));
    }
    
    @Test
    public void checkThatCanGetOnlineResourceWhichExists() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        List<OnlineResource> resources = Arrays.asList(OnlineResource.builder().url("a").build());
        when(document.getOnlineResources()).thenReturn(resources);
        
        //When
        OnlineResource resource = controller.getOnlineResource(document, 0);
        
        //Then
        assertThat("the online resource url is a", resource.getUrl(), equalTo("a"));
    }
    
    @Test(expected=NoSuchOnlineResourceException.class)
    public void checkThatFailsWithExceptionIfResourceIsRequestedWhichIsNotPresent() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getOnlineResources()).thenReturn(Collections.EMPTY_LIST);
        
        //When
        OnlineResource resource = controller.getOnlineResource(document, 0);
        
        //Then
        fail("Expected to fail with execption");
    }
    
    @Test(expected=NoSuchOnlineResourceException.class)
    public void checkThatFailsWithExceptionIfResourceIsRequestedWhichIsNegative() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getOnlineResources()).thenReturn(Collections.EMPTY_LIST);
        
        //When
        OnlineResource resource = controller.getOnlineResource(document, -10);
        
        //Then
        fail("Expected to fail with execption");
    }
    
    @Test(expected=NoSuchOnlineResourceException.class)
    public void checkThatFailsToGetOnlineResourcesFromUnknownMetadataDocumentType() {
        //Given
        MetadataDocument document = mock(MetadataDocument.class);
        
        //When
        OnlineResource resource = controller.getOnlineResource(document, 0);
        
        //Then
        fail("Expected an NoSuchOnlineResourceException when dealing with an unknown document type");
    }
    
    @Test
    public void checkThatGettingOnlineResourceDelegatesToDocumentReader() throws IOException, UnknownContentTypeException {
        //Given
        String file = "bob";
        String revision = "bob";
        int index = 10;
        
        OnlineResource resource = OnlineResource.builder().url("a").build();
        doReturn(resource).when(controller).getOnlineResource(any(GeminiDocument.class), anyInt());
        
        //When
        controller.getOnlineResource(file, revision, index);
        
        //Then
        verify(documentBundleReader).readBundle(file, revision);
    }
    
    @Test
    public void checkThatProcessOrRedirectToOnlineResourceDelegates() throws DataRepositoryException, IOException, UnknownContentTypeException {
        //Given
        String file = "my filename";
        int index = 1;
        
        DataRevision revision = mock(DataRevision.class);
        when(revision.getRevisionID()).thenReturn("12");
        when(repo.getLatestRevision()).thenReturn(revision);
        
        doReturn(null).when(controller).processOrRedirectToOnlineResource("12", file, index);
        
        //When
        controller.processOrRedirectToOnlineResource(file, index);
        
        //Then
        verify(controller).processOrRedirectToOnlineResource("12", file, index);
    }
    
    @Test
    public void checkThatGetCapabilitesResourceIsProcessed() throws IOException, UnknownContentTypeException {
        //Given
        String file = "file";
        String revision = "revision";
        int index = 10;
        
        GeminiDocument geminiDocument = mock(GeminiDocument.class);
        when(documentBundleReader.readBundle(file, revision)).thenReturn(geminiDocument);
        
        OnlineResource onlineResource = OnlineResource.builder().url("http://wms?REQUEST=GetCapabilities").build();
        doReturn(onlineResource).when(controller).getOnlineResource(geminiDocument, index);
        
        WmsCapabilities wmsCapabilities = mock(WmsCapabilities.class);
        doReturn(wmsCapabilities).when(getCapabilitiesObtainerService).getWmsCapabilities(onlineResource);
        
        //When
        Object result = controller.processOrRedirectToOnlineResource(revision, file, index);
        
        //Then
        assertEquals("Expected to the mocked wms capabilities", result, wmsCapabilities);
    }
    
    @Test
    public void checkThatOtherResourceIsRedirectedTo() throws IOException, UnknownContentTypeException {
        //Given
        String file = "file";
        String revision = "revision";
        int index = 10;
        
        GeminiDocument geminiDocument = mock(GeminiDocument.class);
        when(documentBundleReader.readBundle(file, revision)).thenReturn(geminiDocument);
        
        OnlineResource onlineResource = OnlineResource.builder().url("random url").build();
        doReturn(onlineResource).when(controller).getOnlineResource(geminiDocument, index);
        
        //When
        RedirectView result = (RedirectView)controller.processOrRedirectToOnlineResource(revision, file, index);
        
        //Then
        assertEquals("Expected to find a redirect view with the correct url", "random url", result.getUrl());
    }
    
    @Test
    public void checkProxyingOfLatestRevisionDelegates() throws DataRepositoryException, IOException, UnknownContentTypeException {
        //Given
        String file = "my filename", layer="wms layer";
        int index = 1, z = 1, x=3, y =2;
        
        DataRevision revision = mock(DataRevision.class);
        when(revision.getRevisionID()).thenReturn("12");
        when(repo.getLatestRevision()).thenReturn(revision);
        
        TransparentProxyView proxy = mock(TransparentProxyView.class);
        doReturn(proxy).when(controller).proxyMapProxyTileRequest("12", file, index, layer, z, x, y);
        
        //When
        TransparentProxyView proxyView = controller.proxyMapProxyTileRequest(file, index, layer, z, x, y);
        
        //Then
        verify(controller).proxyMapProxyTileRequest("12", file, index, layer, z, x, y);
        assertThat("expected mocked response to be passed through", proxyView, equalTo(proxy));
    }
    
    @Test
    public void checkThatTMSProxies() throws IOException, UnknownContentTypeException {
        //Given        
        String file = "file";
        int index = 2;
        String layerName = "layer";
        
        OnlineResource onlineResource = OnlineResource.builder().url("http://wms?REQUEST=GetCapabilities").build();
        doReturn(onlineResource).when(controller).getOnlineResource(any(MetadataDocument.class), anyInt());
        
        Layer layer = mock(Layer.class);
        when(layer.getName()).thenReturn(layerName);
        when(layer.getLegendUrl()).thenReturn("http://wwww.whereever.com/legend.png");
        
        WmsCapabilities wmsCapabilities = mock(WmsCapabilities.class);
        when(wmsCapabilities.getLayers()).thenReturn(Arrays.asList(layer));
        
        doReturn(wmsCapabilities).when(getCapabilitiesObtainerService).getWmsCapabilities(onlineResource);
        
        //When
        TransparentProxyView proxy = controller.getMapLayerLegend("12", file, index, layerName);
        
        //Then
        assertThat("Expected url to proxy mapProxy", "http://wwww.whereever.com/legend.png", equalTo(proxy.getUrl()));
    }
    
    @Test
    public void checkThatLatestLegendUrlDelegatesToRevision() throws IOException, UnknownContentTypeException {
        //Given
        String file = "file";
        int index = 2;
        String layer = "layer";
        
        DataRevision dataRevision = mock(DataRevision.class);
        when(dataRevision.getRevisionID()).thenReturn("12");
        when(repo.getLatestRevision()).thenReturn(dataRevision);
        
        TransparentProxyView proxy = mock(TransparentProxyView.class);
        doReturn(proxy).when(controller).getMapLayerLegend("12", file, index, layer);
        
        //When
        TransparentProxyView mapLayerLegendView = controller.getMapLayerLegend(file, index, layer);
        
        //Then
        assertThat("Expected the call to the map legend to be delegated", proxy, equalTo(mapLayerLegendView));
    }
    
    @Test
    public void checkThatGetLegendUrlIsProxied() throws IOException, UnknownContentTypeException {
        //Given
        String revision = "revision";
        String file = "file";
        int index = 2;
        String layerName = "layer";
        
        OnlineResource onlineResource = OnlineResource.builder().url("http://wms?REQUEST=GetCapabilities").build();
        doReturn(onlineResource).when(controller).getOnlineResource(any(MetadataDocument.class), anyInt());
        
        Layer layer = mock(Layer.class);
        when(layer.getName()).thenReturn(layerName);
        when(layer.getLegendUrl()).thenReturn("http://wwww.whereever.com/legend.png");
        
        WmsCapabilities wmsCapabilities = mock(WmsCapabilities.class);
        when(wmsCapabilities.getLayers()).thenReturn(Arrays.asList(layer));
        
        doReturn(wmsCapabilities).when(getCapabilitiesObtainerService).getWmsCapabilities(onlineResource);
        
        //When
        TransparentProxyView proxy = controller.getMapLayerLegend(revision, file, index, layerName);
        
        //Then
        assertThat("Expected to proxy the legend url", proxy.getUrl(), equalTo("http://wwww.whereever.com/legend.png") );
    }
    
    @Test(expected=LegendGraphicMissingException.class)
    public void checkThatExceptionIsThrownWhenNoLegendGraphicIsPresentForGivenLayer() throws IOException, UnknownContentTypeException {
        //Given
        String revision = "revision";
        String file = "file";
        int index = 2;
        String layerName = "layer";
        
        OnlineResource onlineResource = OnlineResource.builder().url("http://wms?REQUEST=GetCapabilities").build();
        doReturn(onlineResource).when(controller).getOnlineResource(any(MetadataDocument.class), anyInt());
        
        Layer layer = mock(Layer.class);
        when(layer.getName()).thenReturn(layerName);
        when(layer.getLegendUrl()).thenReturn(null);
        
        WmsCapabilities wmsCapabilities = mock(WmsCapabilities.class);
        when(wmsCapabilities.getLayers()).thenReturn(Arrays.asList(layer));
        
        doReturn(wmsCapabilities).when(getCapabilitiesObtainerService).getWmsCapabilities(onlineResource);
        
        //When
        controller.getMapLayerLegend(revision, file, index, layerName);
        
        //Then
        fail("Expected to fail with legend graphic missing exception");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void checkThatIllegalArgumentExceptionIsThrownIfLayerDoesNotExistWhenGettingLegend() throws IOException, UnknownContentTypeException {
        //Given
        String revision = "revision";
        String file = "file";
        int index = 2;
        String layerName = "layer";
        
        OnlineResource onlineResource = OnlineResource.builder().url("http://wms?REQUEST=GetCapabilities").build();
        doReturn(onlineResource).when(controller).getOnlineResource(any(MetadataDocument.class), anyInt());

        
        WmsCapabilities wmsCapabilities = mock(WmsCapabilities.class);
        when(wmsCapabilities.getLayers()).thenReturn(Collections.EMPTY_LIST);
        
        doReturn(wmsCapabilities).when(getCapabilitiesObtainerService).getWmsCapabilities(onlineResource);
        
        //When
        controller.getMapLayerLegend(revision, file, index, layerName);
        
        //Then
        fail("Expected to fail with illegal argument exception");
    }
}
