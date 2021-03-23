package uk.ac.ceh.gateway.catalogue.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
import uk.ac.ceh.gateway.catalogue.services.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class OnlineResourceControllerTest {
    @Mock BundledReaderService<MetadataDocument> documentBundleReader;
    @Mock GetCapabilitiesObtainerService getCapabilitiesObtainerService;
    @Mock TMSToWMSGetMapService tmsToWMSGetMapService;
    @Mock MapServerDetailsService mapServerDetailsService;
    
    private OnlineResourceController controller;
    
    @BeforeEach
    public void createOnlineController() {
        MockitoAnnotations.initMocks(this);
        
        controller = spy(new OnlineResourceController(documentBundleReader, getCapabilitiesObtainerService, tmsToWMSGetMapService, mapServerDetailsService));
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

//    @org.junit.jupiter.api.Test
//    public void checkThatFailsWithExceptionIfResourceIsRequestedWhichIsNotPresent() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException  {
//
//        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
//            //Given
//            String file = "file";
//            String revision = "revision";
//            doReturn(Collections.EMPTY_LIST).when(controller).getOnlineResources(revision, file);
//
//            //When
//            OnlineResource resource = controller.getOnlineResource(revision, file, 0);
//
//            //Then
//            fail("Expected to fail with execption");
//        });
//    }

    @Test
    public void checkThatFailsWithExceptionIfResourceIsRequestedWhichIsNegative() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            //Given
            String file = "file";
            String revision = "revision";
            doReturn(Collections.EMPTY_LIST).when(controller).getOnlineResources(revision, file);

            //When
            OnlineResource resource = controller.getOnlineResource(file, revision, -10);

            //Then
            fail("Expected to fail with execption");
        });
    }
    
    @Test
    public void checkThatFailsToGetOnlineResourcesFromUnknownMetadataDocumentType() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            //Given
            MetadataDocument document = mock(MetadataDocument.class);
            String file = "file";
            String revision = "revision";
            doReturn(document).when(documentBundleReader).readBundle(revision, file);

            //When
            OnlineResource resource = controller.getOnlineResource(revision, file, 0);

            //Then
            fail("Expected an NoSuchOnlineResourceException when dealing with an unknown document type");

        });
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
    public void checkThatGetCapabilitesResourceIsProcessed() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException, URISyntaxException {
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
        assertEquals(result, wmsCapabilities);
    }
    
    @Test
    public void checkThatOtherResourceIsRedirectedTo() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException, URISyntaxException {
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
        assertEquals("random url", result.getUrl());
    }
       
    @Test
    public void checkThatGetLegendUrlIsProxied() throws IOException, UnknownContentTypeException, URISyntaxException, DataRepositoryException, PostProcessingException {
        //Given
        String revision = "revision";
        String file = "file";
        int index = 2;
        String layerName = "layer";
        String legendOrig = "http://wwww.whereever.com/legend.png";
        String legendRewrite = "http://wwww.somewhereelse.com/legend.png";
        
        OnlineResource onlineResource = OnlineResource.builder().url("http://wms?REQUEST=GetCapabilities").build();
        doReturn(onlineResource).when(controller).getOnlineResource(eq(revision), eq(file), anyInt());
        
        Layer layer = mock(Layer.class);
        when(layer.getName()).thenReturn(layerName);
        when(layer.getLegendUrl()).thenReturn(legendOrig);
        when(mapServerDetailsService.rewriteToLocalWmsRequest(legendOrig)).thenReturn(legendRewrite);
        WmsCapabilities wmsCapabilities = mock(WmsCapabilities.class);
        when(wmsCapabilities.getLayers()).thenReturn(Arrays.asList(layer));
        
        doReturn(wmsCapabilities).when(getCapabilitiesObtainerService).getWmsCapabilities(onlineResource);
        
        //When
        TransparentProxy proxy = controller.getMapLayerLegend(revision, file, index, layerName);
        
        //Then
        assertThat("Expected to proxy the legend url", proxy.getUri().toString(), equalTo(legendRewrite) );
    }
    
    @Test
    public void checkThatExceptionIsThrownWhenNoLegendGraphicIsPresentForGivenLayer() throws IOException, UnknownContentTypeException, URISyntaxException, DataRepositoryException, PostProcessingException {
        Assertions.assertThrows(LegendGraphicMissingException.class, () -> {
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
        });
    }
    
    @Test
    public void checkThatIllegalArgumentExceptionIsThrownIfLayerDoesNotExistWhenGettingLegend() throws IOException, UnknownContentTypeException, URISyntaxException, DataRepositoryException, PostProcessingException {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
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
        });
    }
}
