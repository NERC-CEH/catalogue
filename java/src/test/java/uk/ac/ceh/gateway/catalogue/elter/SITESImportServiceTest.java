package uk.ac.ceh.gateway.catalogue.elter;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("LinkedDocumentRetrievalService")
public class LinkedDocumentRetrievalServiceTest {
    private LinkedDocumentRetrievalService linkedDocumentRetrievalService;
    private MockRestServiceServer mockServer;
    private byte[] success;

    @BeforeEach
    @SneakyThrows
    public void setup() {
        val restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        linkedDocumentRetrievalService = new LinkedDocumentRetrievalService(
                restTemplate
        );
        success = IOUtils.toByteArray(getClass().getResource("example-eidc-record-response.json"));
    }

    @Test
    public void getLinkedRecord() {
        //given
        mockServer
                .expect(requestTo(equalTo("https://catalogue.ceh.ac.uk/documents/fc9bcd1c-e3fc-4c5a-b569-2fe62d40f2f5")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(success, MediaType.APPLICATION_JSON));

        //when
        val actual = linkedDocumentRetrievalService.get("https://catalogue.ceh.ac.uk/documents/fc9bcd1c-e3fc-4c5a-b569-2fe62d40f2f5");

        //then
        mockServer.verify();
        assertThat("Should have retrieved document", actual.getClass(), equalTo(ElterDocument.class));
    }
}
