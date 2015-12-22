package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.LegendGraphicMissingException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.model.TransparentProxy;
import uk.ac.ceh.gateway.catalogue.ogc.Layer;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.GetCapabilitiesObtainerService;
import uk.ac.ceh.gateway.catalogue.services.TMSToWMSGetMapService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
public class OnlineResourceControllerTest {
    @Mock BundledReaderService<MetadataDocument> documentBundleReader;
    @Mock GetCapabilitiesObtainerService getCapabilitiesObtainerService;
    @Mock TMSToWMSGetMapService tmsToWMSGetMapService;
    
    private OnlineResourceController controller;
    
    @Before
    public void createOnlineController() {
        MockitoAnnotations.initMocks(this);
        
        controller = spy(new OnlineResourceController(documentBundleReader, getCapabilitiesObtainerService, tmsToWMSGetMapService));
    }
        
    @Test
    public void checkThatCanGetOnlineResourceWhichExists() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String file = "file";
        String revision = "revision";
        List<OnlineResource> resources = Arrays.asList(OnlineResource.builder().url("a").build());
        doReturn(resources).when(controller).getOnlineResources(revision, file);
        
        //When
        OnlineResource resource = controller.getOnlineResource(revision, file, 0);
        
        //Then
        assertThat("the online resource url is a", resource.getUrl(), equalTo("a"));
    }
    
    @Test(expected=ResourceNotFoundException.class)
    public void checkThatFailsWithExceptionIfResourceIsRequestedWhichIsNotPresent() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException  {
        //Given
        String file = "file";
        String revision = "revision";
        doReturn(Collections.EMPTY_LIST).when(controller).getOnlineResources(revision, file);
        
        //When
        OnlineResource resource = controller.getOnlineResource(revision, file, 0);
        
        //Then
        fail("Expected to fail with execption");
    }
    
    @Test(expected=ResourceNotFoundException.class)
    public void checkThatFailsWithExceptionIfResourceIsRequestedWhichIsNegative() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException  {
        //Given
        String file = "file";
        String revision = "revision";
        doReturn(Collections.EMPTY_LIST).when(controller).getOnlineResources(revision, file);
        
        //When
        OnlineResource resource = controller.getOnlineResource(file, revision, -10);
        
        //Then
        fail("Expected to fail with execption");
    }
    
    @Test(expected=ResourceNotFoundException.class)
    public void checkThatFailsToGetOnlineResourcesFromUnknownMetadataDocumentType() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        MetadataDocument document = mock(MetadataDocument.class);
        String file = "file";
        String revision = "revision";
        doReturn(document).when(documentBundleReader).readBundle(revision, file);
        
        //When
        OnlineResource resource = controller.getOnlineResource(revision, file, 0);
        
        //Then
        fail("Expected an NoSuchOnlineResourceException when dealing with an unknown document type");
    }
    
    @Test
    public void checkThatGettingOnlineResourcesDelegatesToDocumentReader() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String file = "bob";
        String revision = "bob";
        GeminiDocument document = mock(GeminiDocument.class);
        when(documentBundleReader.readBundle(revision, file)).thenReturn(document);
        
        //When
        controller.getOnlineResources(revision, file);
        
        //Then
        verify(documentBundleReader).readBundle(revision, file);
    }
    
    @Test
    public void checkThatGetCapabilitesResourceIsProcessed() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String file = "file";
        String revision = "revision";
        int index = 10;
        
        GeminiDocument geminiDocument = mock(GeminiDocument.class);
        when(documentBundleReader.readBundle(file, revision)).thenReturn(geminiDocument);
        
        OnlineResource onlineResource = OnlineResource.builder().url("http://wms?REQUEST=GetCapabilities&SERVICE=WMS").build();
        doReturn(onlineResource).when(controller).getOnlineResource(revision, file, index);
        
        WmsCapabilities wmsCapabilities = mock(WmsCapabilities.class);
        doReturn(wmsCapabilities).when(getCapabilitiesObtainerService).getWmsCapabilities(onlineResource);
        
        //When
        Object result = controller.processOrRedirectToOnlineResource(revision, file, index);
        
        //Then
        assertEquals("Expected to the mocked wms capabilities", result, wmsCapabilities);
    }
    
    @Test
    public void checkThatOtherResourceIsRedirectedTo() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String file = "file";
        String revision = "revision";
        int index = 10;
        
        GeminiDocument geminiDocument = mock(GeminiDocument.class);
        when(documentBundleReader.readBundle(revision, file)).thenReturn(geminiDocument);
        
        OnlineResource onlineResource = OnlineResource.builder().url("random url").build();
        doReturn(onlineResource).when(controller).getOnlineResource(revision, file, index);
        
        //When
        RedirectView result = (RedirectView)controller.processOrRedirectToOnlineResource(revision, file, index);
        
        //Then
        assertEquals("Expected to find a redirect view with the correct url", "random url", result.getUrl());
    }
    
    @Test
    public void checkThatTMSProxies() throws IOException, UnknownContentTypeException, URISyntaxException, DataRepositoryException, PostProcessingException {
        //Given        
        String file = "file";
        int index = 2;
        String layerName = "layer";
        
        OnlineResource onlineResource = OnlineResource.builder().url("http://wms?REQUEST=GetCapabilities").build();
        doReturn(onlineResource).when(controller).getOnlineResource(any(String.class), eq(file), anyInt());
        
        Layer layer = mock(Layer.class);
        when(layer.getName()).thenReturn(layerName);
        when(layer.getLegendUrl()).thenReturn("http://wwww.whereever.com/legend.png");
        
        WmsCapabilities wmsCapabilities = mock(WmsCapabilities.class);
        when(wmsCapabilities.getLayers()).thenReturn(Arrays.asList(layer));
        
        doReturn(wmsCapabilities).when(getCapabilitiesObtainerService).getWmsCapabilities(onlineResource);
        
        //When
        TransparentProxy proxy = controller.getMapLayerLegend("12", file, index, layerName);
        
        //Then
        assertThat("Expected url to proxy mapProxy", "http://wwww.whereever.com/legend.png", equalTo(proxy.getUri().toString()));
    }
       
    @Test
    public void checkThatGetLegendUrlIsProxied() throws IOException, UnknownContentTypeException, URISyntaxException, DataRepositoryException, PostProcessingException {
        //Given
        String revision = "revision";
        String file = "file";
        int index = 2;
        String layerName = "layer";
        
        OnlineResource onlineResource = OnlineResource.builder().url("http://wms?REQUEST=GetCapabilities").build();
        doReturn(onlineResource).when(controller).getOnlineResource(eq(revision), eq(file), anyInt());
        
        Layer layer = mock(Layer.class);
        when(layer.getName()).thenReturn(layerName);
        when(layer.getLegendUrl()).thenReturn("http://wwww.whereever.com/legend.png");
        
        WmsCapabilities wmsCapabilities = mock(WmsCapabilities.class);
        when(wmsCapabilities.getLayers()).thenReturn(Arrays.asList(layer));
        
        doReturn(wmsCapabilities).when(getCapabilitiesObtainerService).getWmsCapabilities(onlineResource);
        
        //When
        TransparentProxy proxy = controller.getMapLayerLegend(revision, file, index, layerName);
        
        //Then
        assertThat("Expected to proxy the legend url", proxy.getUri().toString(), equalTo("http://wwww.whereever.com/legend.png") );
    }
    
    @Test(expected=LegendGraphicMissingException.class)
    public void checkThatExceptionIsThrownWhenNoLegendGraphicIsPresentForGivenLayer() throws IOException, UnknownContentTypeException, URISyntaxException, DataRepositoryException, PostProcessingException {
        //Given
        String revision = "revision";
        String file = "file";
        int index = 2;
        String layerName = "layer";
        
        OnlineResource onlineResource = OnlineResource.builder().url("http://wms?REQUEST=GetCapabilities").build();
        doReturn(onlineResource).when(controller).getOnlineResource(eq(revision), eq(file), anyInt());
        
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
    public void checkThatIllegalArgumentExceptionIsThrownIfLayerDoesNotExistWhenGettingLegend() throws IOException, UnknownContentTypeException, URISyntaxException, DataRepositoryException, PostProcessingException {
        //Given
        String revision = "revision";
        String file = "file";
        int index = 2;
        String layerName = "layer";
        
        OnlineResource onlineResource = OnlineResource.builder().url("http://wms?REQUEST=GetCapabilities").build();
        doReturn(onlineResource).when(controller).getOnlineResource(eq(revision), eq(file), anyInt());

        
        WmsCapabilities wmsCapabilities = mock(WmsCapabilities.class);
        when(wmsCapabilities.getLayers()).thenReturn(Collections.EMPTY_LIST);
        
        doReturn(wmsCapabilities).when(getCapabilitiesObtainerService).getWmsCapabilities(onlineResource);
        
        //When
        controller.getMapLayerLegend(revision, file, index, layerName);
        
        //Then
        fail("Expected to fail with illegal argument exception");
    }
}
