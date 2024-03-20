package uk.ac.ceh.gateway.catalogue.services;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
public class FusekiExportServiceTest {
    private FusekiExportService service;
    @Mock private DocumentsToTurtleService documentsToTurtleService;
    private MockRestServiceServer mockServer;

    private static final String BASE_URI = "http://catalogue.invalid/";
    private static final List<String> FUSEKI_CATALOGUE_IDS = List.of("eidc", "ukeof");
    private static final String FUSEKI_URL = "http://fuseki.invalid/";
    private static final String FUSEKI_USERNAME = "username";
    private static final String FUSEKI_PASSWORD = "password";

    private final Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

    @SneakyThrows
    @BeforeEach
    void setup() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        configuration.setDirectoryForTemplateLoading(new File("../templates"));

        service = new FusekiExportService(
            documentsToTurtleService,
            restTemplate,
            BASE_URI,
            FUSEKI_CATALOGUE_IDS,
            FUSEKI_URL,
            FUSEKI_USERNAME,
            FUSEKI_PASSWORD
        );
    }

    @Test
    @SneakyThrows
    void exportDocuments() {
        // given
        given(documentsToTurtleService.getBigTtl(any()))
            .willReturn(Optional.of("ttl"));

        mockServer
            .expect(requestTo(equalTo(FUSEKI_URL + "?graph=" + BASE_URI)))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, "text/turtle"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dXNlcm5hbWU6cGFzc3dvcmQ="))
            .andExpect(content().string(equalTo("ttl\nttl")))
            .andRespond(withSuccess());

        // when
        service.runExport();

        // then
        mockServer.verify();
        verify(documentsToTurtleService).getBigTtl(FUSEKI_CATALOGUE_IDS.get(0));
        verify(documentsToTurtleService).getBigTtl(FUSEKI_CATALOGUE_IDS.get(1));
    }

    @Test
    @SneakyThrows
    void doNotExportDocuments() {
        // given
        given(documentsToTurtleService.getBigTtl(any()))
            .willReturn(Optional.empty());

        // when
        service.runExport();

        // then
        mockServer.verify();
        verify(documentsToTurtleService).getBigTtl(FUSEKI_CATALOGUE_IDS.get(0));
        verify(documentsToTurtleService).getBigTtl(FUSEKI_CATALOGUE_IDS.get(1));
    }
}
