/*
 * Copyright (C) 2016 cjohn
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package uk.ac.ceh.gateway.catalogue.ogc;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.ac.ceh.gateway.catalogue.ogc.MapViewerController.INFO_FORMAT;

@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("MapViewerController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})

public @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class MapViewerControllerTest extends AbstractMvcTest {
    @MockitoBean @Qualifier("wms") private RestTemplate rest;
    @MockitoBean private CatalogueService catalogueService;
    @MockitoBean private ClientHttpRequestFactory proxyRequestFactory;
    @MockitoBean private ProfileService profileService;

    @Autowired private Configuration configuration;

    private final String file = "1234-5678";

    private void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue())
            .willReturn(
                    Catalogue.builder()
                    .id("default")
                    .title("test")
                    .url("https://example.com")
                    .contactUrl("")
                    .logo("eidc.png")
                    .build()
                    );
    }

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("profile", profileService);
    }

    private void givenTransparentProxyResponse(String contentType) throws IOException {
        val proxyRequest = mock(ClientHttpRequest.class);
        val response = mock(ClientHttpResponse.class);
        val responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_TYPE, contentType);
        given(response.getHeaders())
            .willReturn(responseHeaders);
        given(response.getBody())
            .willReturn(mock(InputStream.class));
        given(proxyRequest.execute())
            .willReturn(response);
        given(proxyRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET)))
            .willReturn(proxyRequest);
    }

    private void givenGetMapResponse() throws IOException {
        givenTransparentProxyResponse(MediaType.IMAGE_PNG_VALUE);
    }

    @SneakyThrows
    private void givenRemoteWmsFeatureInfo() {
        givenTransparentProxyResponse("application/vnd.ogc.xml");
    }

    private void givenWmsFeatureInfo() {
        val attributes = new HashMap<String, String>();
        attributes.put("bar", "green");
        attributes.put("foo", "red");
        val feature = new WmsFeatureInfo.Layer.Feature();
        feature.setAttributes(attributes);
        val layer = new WmsFeatureInfo.Layer();
        layer.setName("foo");
        layer.setFeatures(Collections.singletonList(feature));
        val wmsFeatureInfo = new WmsFeatureInfo();
        wmsFeatureInfo.setLayers(Collections.singletonList(layer));
        given(rest.getForObject(any(URI.class), eq(WmsFeatureInfo.class)))
            .willReturn(wmsFeatureInfo);
    }

    @Test
    @SneakyThrows
    void getMapViewer() {
        //given
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();

        //when
        mvc.perform(
                get("/maps")
                )
            .andExpect(status().isOk())
            .andExpect(view().name("/html/mapviewer"));
    }

    @Test
    @SneakyThrows
    void getMapRequest() {
        //Given
        givenGetMapResponse();

        //When
        mvc.perform(
                get("/maps/{file}", file)
                .queryParam("SERVICE", "WMS")
                .queryParam("VERSION", "1.3.0")
                .queryParam("REQUEST", "GetMap")
                .queryParam("LAYERS", "layer0", "layer1")
                .queryParam("STYLES", "default")
                .queryParam("CRS", "EPSG:27700")
                .queryParam("BBOX", "-145.15,21.73,-57.15,58.96")
                .queryParam("WIDTH", "250")
                .queryParam("HEIGHT", "250")
                .queryParam("FORMAT", "image/png")
                )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    @SneakyThrows
    void getMapRequestLowercaseParameters() {
        // It is the 'format' parameter that causes trouble with content negotiation
        //Given
        givenGetMapResponse();

        //When
        mvc.perform(
                get("/maps/{file}", file)
                .queryParam("service", "WMS")
                .queryParam("version", "1.3.0")
                .queryParam("request", "GetMap")
                .queryParam("layers", "layer0", "layer1")
                .queryParam("styles", "default")
                .queryParam("crs", "EPSG:27700")
                .queryParam("bbox", "-145.15,21.73,-57.15,58.96")
                .queryParam("width", "250")
                .queryParam("height", "250")
                .queryParam("format", "image/png")
                )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    @SneakyThrows
    void getFeatureInfoRequest() {
        //given
        givenWmsFeatureInfo();

        //when
        mvc.perform(
                get("/maps/{file}", file)
                .queryParam("SERVICE", "WMS")
                .queryParam("VERSION", "1.3.0")
                .queryParam("REQUEST", "GetFeatureInfo")
                .queryParam("LAYERS", "layer0", "layer1")
                .queryParam("STYLES", "default")
                .queryParam("CRS", "EPSG:27700")
                .queryParam("BBOX", "-145.15,21.73,-57.15,58.96")
                .queryParam("WIDTH", "250")
                .queryParam("HEIGHT", "250")
                .queryParam("QUERY_LAYERS", "layer0")
                .queryParam("I", "10")
                .queryParam("J", "20")
                .queryParam(INFO_FORMAT, "text/xml")
                )
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/xml"))
            .andExpect(content().xml("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><FeatureInfoResponse><FIELDS bar=\"green\" foo=\"red\"/></FeatureInfoResponse>"));

        //then
    }

    @Test
    @SneakyThrows
    public void getRemoteGetFeatureInfoRequest() {
        //given
        givenRemoteWmsFeatureInfo();

        //when
        mvc.perform(
                get("/maps/{file}", file)
                .queryParam("SERVICE", "WMS")
                .queryParam("VERSION", "1.3.0")
                .queryParam("REQUEST", "GetFeatureInfo")
                .queryParam("LAYERS", "layer0", "layer1")
                .queryParam("STYLES", "default")
                .queryParam("CRS", "EPSG:27700")
                .queryParam("BBOX", "-145.15,21.73,-57.15,58.96")
                .queryParam("WIDTH", "250")
                .queryParam("HEIGHT", "250")
                .queryParam("QUERY_LAYERS", "layer0")
                .queryParam("I", "10")
                .queryParam("J", "20")
                .queryParam(INFO_FORMAT, "application/vnd.ogc.xml")
                )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/vnd.ogc.xml"));

        //then
        verifyNoInteractions(rest);
    }

    @Test
    @DisplayName("rejects a GetMap larger than the maximum image size")
    @SneakyThrows
    void rejectsGetMapExceedingMaximumImageSize() {
        //when
        mvc.perform(
                get("/maps/{file}", file)
                .queryParam("SERVICE", "WMS")
                .queryParam("VERSION", "1.3.0")
                .queryParam("REQUEST", "GetMap")
                .queryParam("LAYERS", "layer0")
                .queryParam("CRS", "EPSG:27700")
                .queryParam("BBOX", "-145.15,21.73,-57.15,58.96")
                .queryParam("WIDTH", "4096")
                .queryParam("HEIGHT", "4096")
                .queryParam("FORMAT", "image/png")
                )
            .andExpect(status().isBadRequest());

        //then
        verifyNoInteractions(rest);
        verifyNoInteractions(proxyRequestFactory);
    }

    @Test
    @DisplayName("rejects an oversized GetMap given lowercase parameters")
    @SneakyThrows
    void rejectsGetMapExceedingMaximumImageSizeLowercase() {
        //when
        mvc.perform(
                get("/maps/{file}", file)
                .queryParam("service", "WMS")
                .queryParam("version", "1.3.0")
                .queryParam("request", "GetMap")
                .queryParam("layers", "layer0")
                .queryParam("crs", "EPSG:27700")
                .queryParam("bbox", "-145.15,21.73,-57.15,58.96")
                .queryParam("width", "4096")
                .queryParam("height", "4096")
                .queryParam("format", "image/png")
                )
            .andExpect(status().isBadRequest());

        //then
        verifyNoInteractions(rest);
        verifyNoInteractions(proxyRequestFactory);
    }

    @Test
    @DisplayName("rejects a GetMap when only one dimension is oversized")
    @SneakyThrows
    void rejectsGetMapWithSingleOversizedDimension() {
        //when
        mvc.perform(
                get("/maps/{file}", file)
                .queryParam("SERVICE", "WMS")
                .queryParam("REQUEST", "GetMap")
                .queryParam("LAYERS", "layer0")
                .queryParam("WIDTH", "256")
                .queryParam("HEIGHT", "2049")
                .queryParam("FORMAT", "image/png")
                )
            .andExpect(status().isBadRequest());

        //then
        verifyNoInteractions(proxyRequestFactory);
    }

    @Test
    @DisplayName("allows a GetMap at exactly the maximum image size")
    @SneakyThrows
    void allowsGetMapAtMaximumImageSize() {
        //given
        givenGetMapResponse();

        //when
        mvc.perform(
                get("/maps/{file}", file)
                .queryParam("SERVICE", "WMS")
                .queryParam("VERSION", "1.3.0")
                .queryParam("REQUEST", "GetMap")
                .queryParam("LAYERS", "layer0")
                .queryParam("CRS", "EPSG:27700")
                .queryParam("BBOX", "-145.15,21.73,-57.15,58.96")
                .queryParam("WIDTH", "2048")
                .queryParam("HEIGHT", "2048")
                .queryParam("FORMAT", "image/png")
                )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    @DisplayName("rejects a GetFeatureInfo larger than the maximum image size")
    @SneakyThrows
    void rejectsGetFeatureInfoExceedingMaximumImageSize() {
        //when
        mvc.perform(
                get("/maps/{file}", file)
                .queryParam("SERVICE", "WMS")
                .queryParam("REQUEST", "GetFeatureInfo")
                .queryParam("LAYERS", "layer0")
                .queryParam("QUERY_LAYERS", "layer0")
                .queryParam("WIDTH", "4096")
                .queryParam("HEIGHT", "4096")
                .queryParam("I", "10")
                .queryParam("J", "20")
                .queryParam(INFO_FORMAT, "text/xml")
                )
            .andExpect(status().isBadRequest());

        //then
        verifyNoInteractions(rest);
    }

    @Test
    @DisplayName("ignores a non-numeric image dimension rather than failing the request")
    @SneakyThrows
    void ignoresNonNumericImageDimension() {
        //given
        givenGetMapResponse();

        //when
        mvc.perform(
                get("/maps/{file}", file)
                .queryParam("SERVICE", "WMS")
                .queryParam("REQUEST", "GetMap")
                .queryParam("LAYERS", "layer0")
                .queryParam("WIDTH", "not-a-number")
                .queryParam("HEIGHT", "250")
                .queryParam("FORMAT", "image/png")
                )
            .andExpect(status().isOk());
    }
}
