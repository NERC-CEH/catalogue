package uk.ac.ceh.gateway.catalogue.datacite;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.DATACITE_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.EIDC_PUBLISHER_USERNAME;
import static uk.ac.ceh.gateway.catalogue.datacite.DataciteController.DATACITE_ROLE;

@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("DataciteController")
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class
})

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class DataciteControllerTest extends AbstractMvcTest {
    @MockitoBean private DocumentRepository documentRepository;
    @MockitoBean private DocumentIdentifierService identifierService;
    @MockitoBean private DataciteService dataciteService;
    @MockitoBean private JenaLookupService jenaLookupService;

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
        gemini.setPublishers(List.of(
            ResponsibleParty.builder()
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

    /**
     * getDoi is the one endpoint that writes {@code application/vnd.api+json}. It used to rely on the
     * JSON converter claiming every {@code +json} type, which also let records answer that type with
     * their internal JSON (dri-one #409).
     */
    @Test
    @WithMockCatalogueUser(
        username=EIDC_PUBLISHER_USERNAME,
        grantedAuthorities=DATACITE_ROLE
    )
    @DisplayName("getDoi still returns the DataCite request as application/vnd.api+json")
    void getDoiAsDataciteJson() throws Exception {
        //given
        givenDocumentRepository();
        given(dataciteService.getDoiMetadata(any(GeminiDocument.class))).willReturn(
            new DataciteRequest(new DataciteRequest.Data("10.5285/1234", null))
        );

        //when
        mvc.perform(
            get("/documents/{file}/datacite/getDoi", file)
            .header("remote-user", EIDC_PUBLISHER_USERNAME)
            .accept(DATACITE_JSON_VALUE)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(DATACITE_JSON_VALUE))
            .andExpect(jsonPath("$.data.id").value("10.5285/1234"))
            .andExpect(jsonPath("$.data.type").value("dois"));
    }
}
