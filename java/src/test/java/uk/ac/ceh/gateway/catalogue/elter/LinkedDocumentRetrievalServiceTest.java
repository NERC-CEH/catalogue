package uk.ac.ceh.gateway.catalogue.elter;

import java.net.URL;

import lombok.SneakyThrows;
import lombok.val;

import org.apache.commons.io.IOUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
@DisplayName("LinkedDocumentRetrievalService")
public class LinkedDocumentRetrievalServiceTest {
    private LinkedDocumentRetrievalService linkedDocumentRetrievalService;
    private MockRestServiceServer mockServer;
    private byte[] catalogueRecordAsJson;
    private String dataciteTestUrl;

    URL exampleRecord = getClass().getResource("example-eidc-record-response.json");

    @Mock
    private RestTemplate unusedRestTemplate;

    @SneakyThrows
    @Test
    public void getCatalogueLinkedRecord() {
        // setup
        val restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        linkedDocumentRetrievalService = new LinkedDocumentRetrievalService(
                restTemplate,
                "http://foo.invalid"
        );
        catalogueRecordAsJson = IOUtils.toByteArray(exampleRecord);

        //given
        mockServer
                .expect(requestTo(equalTo("https://catalogue.ceh.ac.uk/documents/fc9bcd1c-e3fc-4c5a-b569-2fe62d40f2f5")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(catalogueRecordAsJson, MediaType.APPLICATION_JSON));

        //when
        val actual = linkedDocumentRetrievalService.get("https://catalogue.ceh.ac.uk/documents/fc9bcd1c-e3fc-4c5a-b569-2fe62d40f2f5");

        //then
        mockServer.verify();
        assertThat("Should have retrieved document", actual.getClass(), equalTo(ElterDocument.class));
    }

    @Test
    public void getDoiLinkedRecord() {
        //setup
        // dubious... but no obvious refactoring springs to mind
        dataciteTestUrl = exampleRecord.toString().substring(0,exampleRecord.toString().lastIndexOf("/"));
        linkedDocumentRetrievalService = new LinkedDocumentRetrievalService(
                unusedRestTemplate,
                dataciteTestUrl
        );
        //given
        String urlContainingDoi = "https://doi.org/10.23728/b2share.b56cd875765a403599859177fced08ae";

        //when
        val actual = linkedDocumentRetrievalService.get(urlContainingDoi);

        //then
        assertThat("Should have retrieved document", actual.getClass(), equalTo(ElterDocument.class));
        assertEquals(
                "TERENO Wüstebach meteorological data",
                actual.getTitle()
                );
        assertEquals(
                "10 minute interval temperature and precipitation in °C and mm from different sensors of a meteorological station.Sensor names are temperature at 2m, Precipitation_Cum10min_OttNRTtotal, Precipitation_Cum10min_OttRTNRT, Precipitation_Cum10min_OttNRT, Precipitation_Cum10min_Ecotech, Precipitation_Cum10min_RainCap",
                actual.getDescription()
                );
        assertEquals(
                "10.23728/b2share.b56cd875765a403599859177fced08ae",
                actual.getImportId()
                );
        assertEquals(
                "signpost",
                actual.getType()
                );
        assertEquals(
                "Level 0",
                actual.getDataLevel()
                );
        verifyNoInteractions(unusedRestTemplate);
    }
}
