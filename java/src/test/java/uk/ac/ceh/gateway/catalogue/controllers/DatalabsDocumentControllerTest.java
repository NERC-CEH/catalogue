package uk.ac.ceh.gateway.catalogue.controllers;

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
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.datalabs.DatalabsDocument;
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
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.DATALABS_JSON_VALUE;

@WithMockCatalogueUser
@ActiveProfiles("test")
@DisplayName("DatalabsDocumentController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=DatalabsDocumentController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class DatalabsDocumentControllerTest {
    @MockBean private DocumentRepository documentRepository;
    @MockBean(name="permission") private PermissionService permissionService;

    @Autowired private MockMvc mvc;

    private final String catalogue = "datalabs";
    private final String id = "123-test";
    private final String requestBody = """
            {
                "title": "Datalabs test",
                "version": "1",
                "masterUrl": "https://example.com/datalabs",
                "owners": ["Abe", "Bee", "Cee"]
            }
            """;

    private void givenUserCanCreate() {
        given(permissionService.userCanCreate(catalogue))
            .willReturn(true);
    }

    private void givenUserCanEdit() {
        given(permissionService.userCanEdit(id))
            .willReturn(true);
    }

    @Test
    public void checkCanCreateDatalabsDocument() throws Exception {
        //Given
        givenUserCanCreate();

        DatalabsDocument document = new DatalabsDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String message = "new DataLabs document";

        given(documentRepository.saveNew(
            any(CatalogueUser.class),
            any(DatalabsDocument.class),
            eq(catalogue),
            eq(message)
        )).willReturn(document);

        //When
        mvc.perform(post("/documents")
            .queryParam("catalogue", catalogue)
            .content(requestBody)
            .contentType(DATALABS_JSON_VALUE)
        )
            .andExpect(status().isCreated());

        //Then
    }

    @Test
    public void checkCanEditDatalabsDocument() throws Exception {
        //Given
        givenUserCanEdit();

        DatalabsDocument document = new DatalabsDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String message = "Edited document: test";

        given(documentRepository.read(id))
            .willReturn(new DatalabsDocument().setMetadata(MetadataInfo.builder().build()));
        given(documentRepository.save(
            any(CatalogueUser.class),
            any(DatalabsDocument.class),
            eq(id),
            eq(message)
        )).willReturn(document);

        //When
        mvc.perform(put("/documents/{id}", id)
            .content(requestBody)
            .contentType(DATALABS_JSON_VALUE)
        ).andExpect(status().isOk());

        //Then
    }
}
