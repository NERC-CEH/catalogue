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
package uk.ac.ceh.gateway.catalogue.controllers;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.ogc.WmsFeatureInfo;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;

import java.io.IOException;
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
import static uk.ac.ceh.gateway.catalogue.controllers.MapViewerController.INFO_FORMAT;

@WithMockCatalogueUser
@ActiveProfiles("test")
@DisplayName("MapViewerController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=MapViewerController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
public class MapViewerControllerTest {
    @MockBean @Qualifier("wms") private RestTemplate rest;
    @MockBean private CatalogueService catalogueService;
    @MockBean private CloseableHttpClient httpClient;
    @MockBean private ProfileService profileService;

    @Autowired private Configuration configuration;
    @Autowired private MockMvc mvc;

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

    private void givenGetMapResponse() throws IOException {
        val response = mock(CloseableHttpResponse.class);
        val entity = mock(HttpEntity.class);
        given(response.getEntity())
            .willReturn(entity);
        given(entity.getContentType())
            .willReturn(new BasicHeader("content-type", MediaType.IMAGE_PNG_VALUE));
        given(httpClient.execute(any(HttpGet.class)))
            .willReturn(response);
    }

    @SneakyThrows
    private void givenRemoteWmsFeatureInfo() {
        val response = mock(CloseableHttpResponse.class);
        val entity = mock(HttpEntity.class);
        given(response.getEntity())
            .willReturn(entity);
        given(entity.getContentType())
            .willReturn(new BasicHeader("content-type", "application/vnd.ogc.xml"));
        given(httpClient.execute(any(HttpGet.class)))
            .willReturn(response);
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
}
