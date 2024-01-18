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
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.model.CodeDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.CODE_JSON_VALUE;

@WithMockCatalogueUser
@ActiveProfiles("test")
@DisplayName("CodeDocumentController")
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class
})
@WebMvcTest(
    controllers=CodeDocumentController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class CodeDocumentControllerTest {
    @MockBean private DocumentRepository documentRepository;
    @MockBean(name="permission") private PermissionService permissionService;
    @Autowired private Configuration configuration;
    @MockBean private CatalogueService catalogueService;

    @Autowired private MockMvc mvc;

    private final String catalogue = "datalabs";
    private final String id = "123-test";
    private final String requestBody = """
            {
                "title": "Datalabs test",
                "version": "1",
                "masterUrl": "https://example.com/datalabs"
            }
            """;

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("permission", permissionService);
    }

    private void givenUserCanCreate() {
        given(permissionService.userCanCreate(catalogue))
            .willReturn(true);
    }

    private void givenUserCanEdit() {
        given(permissionService.userCanEdit(id))
            .willReturn(true);
    }

    private void givenCatalogue() {
        given(catalogueService.retrieve(catalogue))
            .willReturn(Catalogue.builder()
                .id(catalogue)
                .title("Foo")
                .url("https://example.com")
                .contactUrl("")
                .logo("eidc.png")
                .build()
            );
    }

    @Test
    public void checkCanCreateCodeDocument() throws Exception {
        //Given
        givenUserCanCreate();
        givenFreemarkerConfiguration();
        givenCatalogue();

        CodeDocument document = new CodeDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        document.setMetadata(MetadataInfo.builder().catalogue(catalogue).build());
        document.setTitle("Test");
        String message = "new code document";

        given(documentRepository.saveNew(
            any(CatalogueUser.class),
            any(CodeDocument.class),
            eq(catalogue),
            eq(message)
        )).willReturn(document);

        //When
        mvc.perform(post("/documents")
            .queryParam("catalogue", catalogue)
            .content(requestBody)
            .contentType(CODE_JSON_VALUE)
        )
            .andExpect(status().isCreated());

        //Then
    }

    @Test
    public void checkCanEditCodeDocument() throws Exception {
        //Given
        givenUserCanEdit();

        CodeDocument document = new CodeDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String message = "Edited document: test";

        given(documentRepository.read(id))
            .willReturn(new CodeDocument().setMetadata(MetadataInfo.builder().build()));
        given(documentRepository.save(
            any(CatalogueUser.class),
            any(CodeDocument.class),
            eq(id),
            eq(message)
        )).willReturn(document);

        //When
        mvc.perform(put("/documents/{id}", id)
            .content(requestBody)
            .contentType(CODE_JSON_VALUE)
        ).andExpect(status().isOk());

        //Then
    }
}
