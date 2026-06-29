package uk.ac.ceh.gateway.catalogue.catalogue;

import lombok.SneakyThrows;
import lombok.val;
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
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import java.util.List;

import static org.mockito.BDDMockito.given;
import org.junit.jupiter.api.BeforeEach;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("CatalogueDocumentController")
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class
})

public @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CatalogueDocumentControllerTest extends AbstractMvcTest {
    private @MockitoBean DocumentRepository documentRepository;
    private @MockitoBean(name="permission") PermissionService permissionService;
    private @MockitoBean CatalogueService catalogueService;

    private final String file = "955b5a6e-dd3f-4b20-a3b5-a9d1d04ba052";

    @BeforeEach
    void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue()).willReturn(
            Catalogue.builder().id("eidc").title("EIDC").url("https://eidc.ceh.ac.uk").contactUrl("").logo("").build()
        );
    }

    @SneakyThrows
    private void givenMetadataDocument() {
        val document = new GeminiDocument();
        document.setId(file);
        document.setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read(file))
            .willReturn(document);
    }

    @SneakyThrows
    private void givenMetadataDocumentWithCatalogueView() {
        val document = new GeminiDocument();
        document.setId(file);
        document.setMetadata(
            MetadataInfo.builder()
                .catalogue("eidc")
                .catalogueView(List.of("ukceh", "assist"))
                .build()
        );
        given(documentRepository.read(file)).willReturn(document);
    }

    private void givenKnownCatalogues() {
        given(catalogueService.retrieveAll()).willReturn(List.of(
            Catalogue.builder().id("eidc").title("EIDC").url("").contactUrl("").logo("").build(),
            Catalogue.builder().id("ukceh").title("UKCEH").url("").contactUrl("").logo("").build(),
            Catalogue.builder().id("assist").title("ASSIST").url("").contactUrl("").logo("").build()
        ));
    }

    private void givenUserCanView() {
        given(permissionService.userCanView(file))
            .willReturn(true);
    }

    private void givenUserCanNotView() {
        given(permissionService.userCanView(file))
            .willReturn(false);
    }

    private void givenUserCanEdit() {
        given(permissionService.userCanEdit(file))
            .willReturn(true);
    }

    @Test
    public void getCurrentCatalogue() throws Exception {
        //Given
        givenUserCanView();
        givenMetadataDocument();
        val expectedResponse = """
            {
                "id": "955b5a6e-dd3f-4b20-a3b5-a9d1d04ba052",
                "value": "eidc"
            }
            """;

        //When
        mvc.perform(
                get("/documents/{file}/catalogue", file)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(expectedResponse));

        //Then
    }

    @SneakyThrows
    @Test
    public void getUnknownFile() {
        //Given
        givenUserCanNotView();

        //When
        mvc.perform(
                get("/documents/{file}/catalogue", file)
            )
            .andExpect(status().isForbidden());

        //Then
    }

    @Test
    public void updateCatalogue() throws Exception {
        //Given
        givenUserCanEdit();
        val user = new CatalogueUser("test","test@eample.com");

        val document = new GeminiDocument()
            .setId(file)
            .setMetadata(
                MetadataInfo.builder()
                .catalogue("eidc")
                .build()
        );
        given(documentRepository.read(file)).willReturn(document);
        given(documentRepository.save(
            user,
            document,
            file,
            "Catalogues of 955b5a6e-dd3f-4b20-a3b5-a9d1d04ba052 changed."
        )).willReturn(document);

        //When
        mvc.perform(
                put("/documents/{file}/catalogue", file)
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {"id": "1", "value": "eidc"}
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON));

        //Then
    }

    @Test
    public void getCurrentCatalogueView() throws Exception {
        //Given
        givenUserCanView();
        givenMetadataDocumentWithCatalogueView();
        val expectedResponse = """
            {
                "id": "955b5a6e-dd3f-4b20-a3b5-a9d1d04ba052",
                "value": ["ukceh", "assist"]
            }
            """;

        //When
        mvc.perform(get("/documents/{file}/catalogue-view", file))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(expectedResponse));
    }

    @Test
    public void getCatalogueViewForbiddenWhenNoViewPermission() throws Exception {
        //Given
        givenUserCanNotView();

        //When
        mvc.perform(get("/documents/{file}/catalogue-view", file))
            .andExpect(status().isForbidden());
    }

    @SneakyThrows
    @Test
    public void updateCatalogueViewFiltersOutPrimaryAndUnknownCatalogues() throws Exception {
        //Given
        givenUserCanEdit();
        givenKnownCatalogues();
        val user = new CatalogueUser("test", "test@example.com");
        val document = new GeminiDocument()
            .setId(file)
            .setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read(file)).willReturn(document);
        given(documentRepository.save(
            user,
            document,
            file,
            "Secondary catalogues of 955b5a6e-dd3f-4b20-a3b5-a9d1d04ba052 changed."
        )).willReturn(document);

        //When — sending "eidc" (primary, filtered out) and "unknown" (not in catalogue service)
        mvc.perform(
            put("/documents/{file}/catalogue-view", file)
                .contentType(APPLICATION_JSON)
                .content("""
                    {"id": "955b5a6e-dd3f-4b20-a3b5-a9d1d04ba052", "value": ["ukceh", "eidc", "unknown"]}
                    """)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON));
    }

}
