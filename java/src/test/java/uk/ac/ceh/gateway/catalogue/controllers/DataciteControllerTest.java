package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
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
    @MockitoBean private DocumentRepository documentRepository;
    @MockitoBean private DocumentIdentifierService identifierService;
    @MockitoBean private DataciteService dataciteService;
    @MockitoBean private JenaLookupService jenaLookupService;

    @Autowired private MockMvc mvc;

    private final String file = "1234";
    private final GeminiDocument gemini = new GeminiDocument();

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

    private void givenGenerateDoi() {
        given(dataciteService.generateDoi(any(GeminiDocument.class)))
            .willReturn(ResourceIdentifier.builder().code(file).codeSpace("doi").build());
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
