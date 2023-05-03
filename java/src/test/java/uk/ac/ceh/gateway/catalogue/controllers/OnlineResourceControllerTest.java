package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.ogc.Layer;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.wms.GetCapabilitiesObtainerService;
import uk.ac.ceh.gateway.catalogue.wms.MapServerDetailsService;
import uk.ac.ceh.gateway.catalogue.wms.TMSToWMSGetMapService;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockCatalogueUser
@Slf4j
@ActiveProfiles("test")
@DisplayName("OnlineResourceController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=OnlineResourceController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
public class OnlineResourceControllerTest {
    @Autowired private MockMvc mvc;

    @MockBean private BundledReaderService<MetadataDocument> documentBundleReader;
    @MockBean private GetCapabilitiesObtainerService getCapabilitiesObtainerService;
    @MockBean private TMSToWMSGetMapService tmsToWMSGetMapService;
    @MockBean private MapServerDetailsService mapServerDetailsService;
    @MockBean private CloseableHttpClient httpClient;

    private final String file = "file";
    private final String revision = "revision";

    private void givenTransparentProxyResponse() throws IOException {
        val response = mock(CloseableHttpResponse.class);
        val entity = mock(HttpEntity.class);
        given(response.getEntity())
            .willReturn(entity);
        given(entity.getContentType())
            .willReturn(new BasicHeader("content-type", MediaType.IMAGE_PNG_VALUE));
        given(httpClient.execute(any(HttpGet.class)))
            .willReturn(response);
    }

    private void givenLocalWmsRequest() {
        given(mapServerDetailsService.rewriteToLocalWmsRequest("foo"))
            .willReturn("bar");
    }

    private void givenWmsCapabilities() {
        WmsCapabilities wmsCapabilities = new WmsCapabilities();
        wmsCapabilities.setDirectFeatureInfo("directFeatureInfo");
        wmsCapabilities.setDirectMap("directMap");
        val layer = new Layer();
        layer.setLegendUrl("foo");
        layer.setName("layer1");
        wmsCapabilities.setLayers(Collections.singletonList(
            layer
        ));
        given(getCapabilitiesObtainerService.getWmsCapabilities(any(OnlineResource.class)))
            .willReturn(wmsCapabilities);
    }

    @SneakyThrows
    private void givenDocumentWithOnlineResource() {
        val document = new GeminiDocument();
        document.setOnlineResources(Collections.singletonList(
            OnlineResource.builder().url("http://example.com/a").build()
        ));
        given(documentBundleReader.readBundle(file)).willReturn(document);
    }

    @SneakyThrows
    private void givenDocumentWithWmsOnlineResource() {
        val document = new GeminiDocument();
        document.setOnlineResources(Collections.singletonList(
            OnlineResource.builder().url("http://example.com/a?request=getcapabilities&service=wms").build()
        ));
        given(documentBundleReader.readBundle(file)).willReturn(document);
    }

    @SneakyThrows
    private void givenHistoricDocumentWithOnlineResource() {
        val document = new GeminiDocument();
        document.setOnlineResources(Collections.singletonList(
            OnlineResource.builder().url("a").build()
        ));
        given(documentBundleReader.readBundle(file, revision)).willReturn(document);
    }

    @SneakyThrows
    private void givenDocumentWithNoOnlineResources() {
        val document = new GeminiDocument();
        document.setOnlineResources(
            Collections.emptyList()
        );
        given(documentBundleReader.readBundle(file, revision))
            .willReturn(document);
    }

    @Test
    @DisplayName("get existing resource")
    @SneakyThrows
    void checkThatCanGetOnlineResourceWhichExists() {
        //Given
        givenDocumentWithOnlineResource();
        val expectedResponse = "[{\"url\":\"http://example.com/a\",\"type\":\"OTHER\"}]";

        //When
        mvc.perform(
            get("/documents/{file}/onlineResources", file)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponse));
    }

    @Test
    @DisplayName("get existing resource at revision")
    @SneakyThrows
    void getOnlineResourceAtRevision() {
        //Given
        givenHistoricDocumentWithOnlineResource();
        val expectedResponse = "[{\"url\":\"a\",\"type\":\"OTHER\"}]";

        //When
        mvc.perform(
            get("/history/{revision}/{file}/onlineResources", revision, file)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponse));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -10})
    @DisplayName("fails if document has no Online Resources")
    @SneakyThrows
    void onlineResourceNotPresent(int index) {
        //Given
        givenDocumentWithNoOnlineResources();

        //When
       mvc.perform(
           get("/history/{revision}/{file}/onlineResources/{index}", revision, file, index)
       )
           .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void getOnlineResource() {
        //given
        givenDocumentWithOnlineResource();

        //when
        mvc.perform(
            get("/documents/{file}/onlineResources/{index}", file, 0)
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("location", "http://example.com/a"));
    }

    @Test
    @SneakyThrows
    void getWmsOnlineResource() {
        //given
        givenDocumentWithWmsOnlineResource();
        givenWmsCapabilities();

        //when
        mvc.perform(
            get("/documents/{file}/onlineResources/{index}", file, 0)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"layers\":[{\"name\":\"layer1\",\"legendUrl\":\"foo\"}],\"directMap\":\"directMap\",\"directFeatureInfo\":\"directFeatureInfo\"}"));
    }

    @Test
    @SneakyThrows
    void getTileRequest() {
        //given
        givenDocumentWithWmsOnlineResource();
        givenWmsCapabilities();
        given(tmsToWMSGetMapService.getWMSMapRequest("directMap", "layer1", 1, 2, 3))
            .willReturn("foo");
        givenLocalWmsRequest();
        givenTransparentProxyResponse();

        //when
        mvc.perform(
            get("/documents/{file}/onlineResources/{index}/tms/1.0.0/{layer}/{z}/{x}/{y}.png", file, 0, "layer1", 1, 2, 3)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    @SneakyThrows
    void checkThatGetLegendUrlIsProxied() {
        //Given
        givenDocumentWithWmsOnlineResource();
        givenWmsCapabilities();
        givenLocalWmsRequest();
        givenTransparentProxyResponse();

        //When
        mvc.perform(
            get("/documents/{file}/onlineResources/{index}/{layer}/legend", file, 0, "layer1")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

}
