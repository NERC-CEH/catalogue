package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.GetCapabilitiesObtainerService;
import uk.ac.ceh.gateway.catalogue.services.MapServerDetailsService;
import uk.ac.ceh.gateway.catalogue.services.TMSToWMSGetMapService;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ActiveProfiles({"development"})
@ContextConfiguration(classes = {
    OnlineResourceController.class,
    DevelopmentUserStoreConfig.class,
    SecurityConfigCrowd.class
})
@DisplayName("OnlineResourceController")
@WebMvcTest(OnlineResourceController.class)
public class OnlineResourceControllerTest {
    @Autowired private MockMvc mockMvc;

    @MockBean private BundledReaderService<MetadataDocument> documentBundleReader;
    @MockBean private GetCapabilitiesObtainerService getCapabilitiesObtainerService;
    @MockBean private TMSToWMSGetMapService tmsToWMSGetMapService;
    @MockBean private MapServerDetailsService mapServerDetailsService;

    private final String file = "file";
    private final String revision = "revision";

    @Nested
    @DisplayName("Online Resources")
    class OnlineResourceTests {

        @Test
        @DisplayName("get existing resource")
        @WithMockUser(username = "unprivileged")
        @SneakyThrows
        void checkThatCanGetOnlineResourceWhichExists() {
            //Given
            val expectedResponse = "[{\"url\":\"a\",\"name\":\"\",\"description\":\"\",\"function\":\"\",\"type\":\"OTHER\"}]";
            val document = new GeminiDocument();
            document.setOnlineResources(Collections.singletonList(
                OnlineResource.builder().url("a").build()
            ));
            given(documentBundleReader.readBundle(file)).willReturn(document);

            //When
            mockMvc.perform(
                get("/documents/file/onlineResources")
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse));
        }

//        @Test
//        @DisplayName("fails if document has no Online Resources")
//        @SneakyThrows
//        void checkThatFailsWithExceptionIfResourceIsRequestedWhichIsNotPresent() {
//
//            Assertions.assertThrows(ResourceNotFoundException.class, () -> {
//                //Given
//                val document = new GeminiDocument();
//                document.setOnlineResources(
//                    Collections.emptyList()
//                );
//                doReturn(document).when(documentBundleReader).readBundle(file, revision);
//
//                //When
//                controller.getOnlineResource(revision, file, 0);
//            });
//
//            //Then
//            verify(documentBundleReader).readBundle(file, revision);
//        }
//
//        @Test
//        @DisplayName("fails if index is negative")
//        @SneakyThrows
//        void checkThatFailsWithExceptionIfResourceIsRequestedWhichIsNegative() {
//            Assertions.assertThrows(ResourceNotFoundException.class, () -> {
//                //Given
//                val document = new GeminiDocument();
//                document.setOnlineResources(
//                    Collections.emptyList()
//                );
//                doReturn(document).when(documentBundleReader).readBundle(file, revision);
//
//                //When
//                controller.getOnlineResource(revision, file, -10);
//
//                //Then
//                verify(documentBundleReader).readBundle(file, revision);
//            });
//
//            //Then
//            verify(documentBundleReader).readBundle(file, revision);
//        }
//
//        @Test
//        @DisplayName("fails if unknown metadata document type")
//        @SneakyThrows
//        void checkThatFailsToGetOnlineResourcesFromUnknownMetadataDocumentType() {
//            Assertions.assertThrows(ResourceNotFoundException.class, () -> {
//                //Given
//                doThrow(ResourceNotFoundException.class).when(documentBundleReader).readBundle(file, revision);
//
//                //When
//                controller.getOnlineResource(revision, file, 0);
//            });
//
//            //Then
//            verify(documentBundleReader).readBundle(file, revision);
//        }
//
//        @Test
//        @DisplayName("just returns a document")
//        @SneakyThrows
//        void checkThatGettingOnlineResourcesDelegatesToDocumentReader() {
//            //Given
//            val document = new GeminiDocument();
//            when(documentBundleReader.readBundle(file, revision)).thenReturn(document);
//
//            //When
//            controller.getOnlineResources(revision, file);
//
//            //Then
//            verify(documentBundleReader).readBundle(file, revision);
//        }
    }

//    @Test
//    @DisplayName("Capabilities Resource is processed")
//    @SneakyThrows
//    public void checkThatGetCapabilitiesResourceIsProcessed() {
//        //Given
//        int index = 10;
//
//        val geminiDocument = new GeminiDocument();
//        geminiDocument.setOnlineResources(Collections.singletonList(
//            OnlineResource.builder().url("http://wms?REQUEST=GetCapabilities&SERVICE=WMS").build()
//        ));
//        when(documentBundleReader.readBundle(file, revision)).thenReturn(geminiDocument);
//
//        WmsCapabilities wmsCapabilities = mock(WmsCapabilities.class);
//        doReturn(wmsCapabilities).when(getCapabilitiesObtainerService).getWmsCapabilities(onlineResource);
//
//        //When
//        controller.processOrRedirectToOnlineResource(revision, file, index);
//    }
//
//    @Test
//    public void checkThatOtherResourceIsRedirectedTo() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException, URISyntaxException {
//        //Given
//        String file = "file";
//        String revision = "revision";
//        int index = 10;
//
//        GeminiDocument geminiDocument = mock(GeminiDocument.class);
//        when(documentBundleReader.readBundle(revision, file)).thenReturn(geminiDocument);
//
//        OnlineResource onlineResource = OnlineResource.builder().url("random url").build();
//        doReturn(onlineResource).when(controller).getOnlineResource(revision, file, index);
//
//        //When
//        RedirectView result = (RedirectView)controller.processOrRedirectToOnlineResource(revision, file, index);
//
//        //Then
//        assertEquals("random url", result.getUrl());
//    }
//
//    @Test
//    public void checkThatGetLegendUrlIsProxied() throws IOException, UnknownContentTypeException, URISyntaxException, DataRepositoryException, PostProcessingException {
//        //Given
//        String revision = "revision";
//        String file = "file";
//        int index = 2;
//        String layerName = "layer";
//        String legendOrig = "http://wwww.whereever.com/legend.png";
//        String legendRewrite = "http://wwww.somewhereelse.com/legend.png";
//
//        OnlineResource onlineResource = OnlineResource.builder().url("http://wms?REQUEST=GetCapabilities").build();
//        doReturn(onlineResource).when(controller).getOnlineResource(eq(revision), eq(file), anyInt());
//
//        Layer layer = mock(Layer.class);
//        when(layer.getName()).thenReturn(layerName);
//        when(layer.getLegendUrl()).thenReturn(legendOrig);
//        when(mapServerDetailsService.rewriteToLocalWmsRequest(legendOrig)).thenReturn(legendRewrite);
//        WmsCapabilities wmsCapabilities = mock(WmsCapabilities.class);
//        when(wmsCapabilities.getLayers()).thenReturn(Arrays.asList(layer));
//
//        doReturn(wmsCapabilities).when(getCapabilitiesObtainerService).getWmsCapabilities(onlineResource);
//
//        //When
//        TransparentProxy proxy = controller.getMapLayerLegend(revision, file, index, layerName);
//
//        //Then
//        assertThat("Expected to proxy the legend url", proxy.getUri().toString(), equalTo(legendRewrite) );
//    }
//
//    @Test
//    public void checkThatExceptionIsThrownWhenNoLegendGraphicIsPresentForGivenLayer() throws IOException, UnknownContentTypeException, URISyntaxException, DataRepositoryException, PostProcessingException {
//        Assertions.assertThrows(LegendGraphicMissingException.class, () -> {
//            //Given
//            String revision = "revision";
//            String file = "file";
//            int index = 2;
//            String layerName = "layer";
//
//            OnlineResource onlineResource = OnlineResource.builder().url("http://wms?REQUEST=GetCapabilities").build();
//            doReturn(onlineResource).when(controller).getOnlineResource(eq(revision), eq(file), anyInt());
//
//            Layer layer = mock(Layer.class);
//            when(layer.getName()).thenReturn(layerName);
//            when(layer.getLegendUrl()).thenReturn(null);
//
//            WmsCapabilities wmsCapabilities = mock(WmsCapabilities.class);
//            when(wmsCapabilities.getLayers()).thenReturn(Arrays.asList(layer));
//
//            doReturn(wmsCapabilities).when(getCapabilitiesObtainerService).getWmsCapabilities(onlineResource);
//
//            //When
//            controller.getMapLayerLegend(revision, file, index, layerName);
//
//            //Then
//            fail("Expected to fail with legend graphic missing exception");
//        });
//    }
//
//    @Test
//    public void checkThatIllegalArgumentExceptionIsThrownIfLayerDoesNotExistWhenGettingLegend() throws IOException, UnknownContentTypeException, URISyntaxException, DataRepositoryException, PostProcessingException {
//        Assertions.assertThrows(IllegalArgumentException.class, () -> {
//            //Given
//            String revision = "revision";
//            String file = "file";
//            int index = 2;
//            String layerName = "layer";
//
//            OnlineResource onlineResource = OnlineResource.builder().url("http://wms?REQUEST=GetCapabilities").build();
//            doReturn(onlineResource).when(controller).getOnlineResource(eq(revision), eq(file), anyInt());
//
//
//            WmsCapabilities wmsCapabilities = mock(WmsCapabilities.class);
//            when(wmsCapabilities.getLayers()).thenReturn(Collections.EMPTY_LIST);
//
//            doReturn(wmsCapabilities).when(getCapabilitiesObtainerService).getWmsCapabilities(onlineResource);
//
//            //When
//            controller.getMapLayerLegend(revision, file, index, layerName);
//
//            //Then
//            fail("Expected to fail with illegal argument exception");
//        });
//    }
}
