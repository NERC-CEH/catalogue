package uk.ac.ceh.gateway.catalogue.services;

import freemarker.template.Configuration;

import lombok.SneakyThrows;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.hamcrest.CoreMatchers.equalTo;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
public class FusekiExportServiceTest {
    private FusekiExportService service;

    @Mock private CatalogueService catalogueService;
    @Mock private DocumentRepository documentRepository;
    @Mock private MetadataListingService listing;
    private MockRestServiceServer mockServer;

    private static final String BASE_URI = "http://catalogue.invalid/";
    private static final String FUSEKI_URL = "http://fuseki.invalid/";
    private static final String FUSEKI_USERNAME = "username";
    private static final String FUSEKI_PASSWORD = "password";
    private static final String CATALOGUE_ID = "testId";
    private static final String CATALOGUE_TITLE = "Test catalogue title";

    private final Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

    private final Catalogue catalogue = Catalogue.builder()
        .id(CATALOGUE_ID)
        .title(CATALOGUE_TITLE)
        .url("n/a")
        .contactUrl("n/a")
        .logo("n/a")
        .build();

    @SneakyThrows
    @BeforeEach
    public void setup() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
        // called in constructor of FusekiExportService
        given(catalogueService.defaultCatalogue())
            .willReturn(catalogue);

        service = new FusekiExportService(
                catalogueService,
                configuration,
                documentRepository,
                listing,
                restTemplate,
                BASE_URI,
                FUSEKI_URL,
                FUSEKI_USERNAME,
                FUSEKI_PASSWORD
                );
    }

    @Test
    @SneakyThrows
    public void exportDocuments() throws DataRepositoryException {
        // given
        mockServer
                .expect(requestTo(equalTo(FUSEKI_URL + "?graph=" + BASE_URI)))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, "text/turtle"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dXNlcm5hbWU6cGFzc3dvcmQ="))
                .andRespond(withSuccess());
        // when
        service.runExport();

        // then
        mockServer.verify();
    }
}
