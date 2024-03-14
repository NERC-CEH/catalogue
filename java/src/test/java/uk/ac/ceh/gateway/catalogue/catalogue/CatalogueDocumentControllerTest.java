package uk.ac.ceh.gateway.catalogue.catalogue;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@ActiveProfiles({"test", "server:eidc"})
@DisplayName("CatalogueDocumentController")
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class
})
@WebMvcTest(CatalogueDocumentController.class)
public class CatalogueDocumentControllerTest {
    private @MockBean DocumentRepository documentRepository;
    private @MockBean(name="permission") PermissionService permissionService;

    @Autowired private MockMvc mvc;

    private final String file = "955b5a6e-dd3f-4b20-a3b5-a9d1d04ba052";

    @SneakyThrows
    private void givenMetadataDocument() {
        val document = new GeminiDocument();
        document.setId(file);
        document.setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read(file))
            .willReturn(document);
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

}
