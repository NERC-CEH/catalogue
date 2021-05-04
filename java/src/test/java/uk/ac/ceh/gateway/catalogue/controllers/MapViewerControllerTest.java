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
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.junit.jupiter.api.BeforeEach;
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
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;
import uk.ac.ceh.gateway.catalogue.services.MapServerDetailsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@DisplayName("MapViewerController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=MapViewerController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
public class MapViewerControllerTest {
    @MockBean @Qualifier("wms") private RestTemplate rest;
    @MockBean private MapServerDetailsService mapServerDetailsService;
    @MockBean private CatalogueService catalogueService;
    @MockBean private CloseableHttpClient httpClient;

    @Autowired private Configuration configuration;
    @Autowired private MockMvc mockMvc;

    private final String file = "1234-5678";
    private final String query = "REQUEST=GetMap&FORMAT=image/png";

    private MapViewerController controller;

    @BeforeEach
    public void init() {
        controller = spy(new MapViewerController(rest, mapServerDetailsService));
    }

    private void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue())
            .willReturn(
                Catalogue.builder()
                    .id("default")
                    .title("test")
                    .url("http://example.com")
                    .build()
            );
    }

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("catalogues", catalogueService);
    }

    private void givenLocalWmsRequest() {
        given(mapServerDetailsService.getLocalWMSRequest(file, query))
            .willReturn("http://mapserver/" + file + "?" + query);
    }

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

    @Test
    @SneakyThrows
    void getMapViewer() {
        //given
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();

        //when
        mockMvc.perform(
            get("/maps")
        )
            .andExpect(status().isOk())
            .andExpect(view().name("/html/mapviewer"));
    }
    
    @Test
    @SneakyThrows
    void checkThatCanProxyToMapServer() {
        //Given
        givenLocalWmsRequest();
        givenTransparentProxyResponse();

        //When
        mockMvc.perform(
            get("/maps/{file}", file)
                .queryParam("REQUEST", "GetMap")
                .queryParam("FORMAT", "image/png")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    public void checkThatCanIdentifyLocalGetFeatureInfoRequest() {
        //Given
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("REQUEST", "GetFeatureInfo"));
        params.add(new BasicNameValuePair("SERVICE", "WMS"));
        params.add(new BasicNameValuePair("INFO_FORMAT", "text/xml"));

        //When
        boolean isLocal = controller.isLocalGetFeatureInfoRequest(params);

        //Then
        assertTrue(isLocal);
    }

    @Test
    public void checkThatCanIdentifyRemoteGetFeatureInfoRequest() {
        //Given
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("REQUEST", "GetFeatureInfo"));
        params.add(new BasicNameValuePair("SERVICE", "WMS"));
        params.add(new BasicNameValuePair("INFO_FORMAT", "application/vnd.ogc.xml"));

        //When
        boolean isLocal = controller.isLocalGetFeatureInfoRequest(params);

        //Then
        assertFalse(isLocal);
    }

    @Test
    public void checkThatCanReplaceTheInfoFormatValue() {
        //Given
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("REQUEST", "GetFeatureInfo"));
        params.add(new BasicNameValuePair("INFO_FORMAT", "text/xml"));

        //When
        String queryString = controller.createQueryStringWithLocalInfoFormat(params, "text/anything");

        //Then
        assertEquals(queryString, "REQUEST=GetFeatureInfo&INFO_FORMAT=text%2Fanything");
    }
}
