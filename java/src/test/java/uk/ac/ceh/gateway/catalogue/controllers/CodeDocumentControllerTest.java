package uk.ac.ceh.gateway.catalogue.controllers;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
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
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.CODE_JSON_VALUE;

@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("CodeDocumentController")
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class
})

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CodeDocumentControllerTest extends AbstractMvcTest {
    @MockitoBean private DocumentRepository documentRepository;
    @MockitoBean(name="permission") private PermissionService permissionService;
    @Autowired private Configuration configuration;
    @MockitoBean private CatalogueService catalogueService;
    @MockitoBean private CachedDataRepository cachedDataRepository;

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
    public void escapesInputsOutputsAndPackagesOnTheRecordPage() throws Exception {
        // dri-one #454: these lists were output with escaping switched off to build links
        givenFreemarkerConfiguration();
        givenCatalogue();
        given(permissionService.toAccess(any(CatalogueUser.class), eq(id), eq("VIEW"))).willReturn(true);

        CodeDocument document = new CodeDocument();
        document.setId(id);
        document.setUri("https://catalogue.ceh.ac.uk/id/" + id);
        document.setMetadata(MetadataInfo.builder().catalogue(catalogue).documentType("CODE_DOCUMENT").build());
        document.setTitle("Test");
        document.setInputs(java.util.List.of("<script>alert(1)</script>", "https://example.com/'onmouseover='alert(1)"));
        given(documentRepository.read(id)).willReturn(document);

        mvc.perform(get("/documents/{id}", id).accept(org.springframework.http.MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                not(containsString("<script>alert(1)</script>")),
                containsString("&lt;script&gt;alert(1)&lt;/script&gt;"),
                not(containsString("href='https://example.com/'onmouseover")),
                containsString("href=\"https://example.com/&#39;onmouseover=&#39;alert(1)\"")
            )));
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
            eq(message),
            eq("rev1")
        )).willReturn(document);

        //When
        mvc.perform(put("/documents/{id}", id)
            .header("If-Match", "\"rev1\"")
            .content(requestBody)
            .contentType(CODE_JSON_VALUE)
        ).andExpect(status().isOk());

        //Then
    }
}
