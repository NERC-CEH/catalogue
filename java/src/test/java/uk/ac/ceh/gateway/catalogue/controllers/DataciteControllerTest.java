package uk.ac.ceh.gateway.catalogue.controllers;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteResponse;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.DATACITE_XML_VALUE;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.EIDC_PUBLISHER_USERNAME;
import static uk.ac.ceh.gateway.catalogue.controllers.DataciteController.DATACITE_ROLE;

@WithMockCatalogueUser
@ActiveProfiles("test")
@DisplayName("DataciteController")
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class
})
@WebMvcTest(
    controllers=DataciteController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class DataciteControllerTest {
    @MockBean private DocumentRepository documentRepository;
    @MockBean private DocumentIdentifierService identifierService;
    @MockBean private DataciteService dataciteService;
    @MockBean private JenaLookupService jenaLookupService;

    @Autowired private MockMvc mvc;
    @Autowired private Configuration configuration;

    private final String file = "1234";
    private final GeminiDocument gemini = new GeminiDocument();


    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("jena", jenaLookupService);
    }

    @SneakyThrows
    private void givenDocumentRepository() {
        gemini.setTitle("Datacite Example");
        gemini.setDescription("Dataset description");
        gemini.setDatasetReferenceDate(DatasetReferenceDate.builder()
            .publicationDate(LocalDate.of(2021, 5, 5))
            .build()
        );
        gemini.setResourceIdentifiers(new ArrayList<>());
        gemini.setResponsibleParties(List.of(
            ResponsibleParty.builder()
                .role("publisher")
                .organisationName("NERC EDS Environmental Information Data Centre")
                .build()
        ));
        given(documentRepository.read(file))
            .willReturn(gemini);
    }

    private void givenDataciteService() {
        given(dataciteService.getDataciteResponse(gemini))
            .willReturn(DataciteResponse.builder()
                .doc(gemini)
                .resourceType("Dataset")
                .doi("10.285/" + file)
                .build()
            );
    }

    private void givenGenerateDoi() {
        given(dataciteService.generateDoi(any(GeminiDocument.class)))
            .willReturn(ResourceIdentifier.builder().code(file).codeSpace("doi").build());
    }

    @SneakyThrows
    private String expectedResponse(String filename) {
        return StreamUtils.copyToString(
            getClass().getResourceAsStream(filename),
            StandardCharsets.UTF_8
        );
    }

    private String expectedDatacite() {
        return expectedResponse("datacite.xml");
    }

    @Test
    void getDataciteXml() throws Exception {
        //given
        givenFreemarkerConfiguration();
        givenDocumentRepository();
        givenDataciteService();

        //when
        mvc.perform(
            get("/documents/{file}/datacite?format=datacite", file)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(DATACITE_XML_VALUE))
            .andExpect(content().xml(expectedDatacite()));
    }

    @Test
    void getDataciteXmlNoAccept() throws Exception {
        //given
        givenFreemarkerConfiguration();
        givenDocumentRepository();
        givenDataciteService();

        //when
        mvc.perform(
            get("/documents/{file}/datacite.xml", file)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(DATACITE_XML_VALUE))
            .andExpect(content().xml(expectedDatacite()));
    }

    @Test
    @WithMockCatalogueUser(
        username=EIDC_PUBLISHER_USERNAME,
        grantedAuthorities=DATACITE_ROLE
    )
    void mintDoi() throws Exception {
        //given
        givenDocumentRepository();
        givenGenerateDoi();
        given(identifierService.generateUri(file)).willReturn("https://example.com/1234");

        //when
        mvc.perform(
            post("/documents/{file}/datacite", file)
            .header("remote-user", EIDC_PUBLISHER_USERNAME)
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("location", "https://example.com/1234"));
    }
}
