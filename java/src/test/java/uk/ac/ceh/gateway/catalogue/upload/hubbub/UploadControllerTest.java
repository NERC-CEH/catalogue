package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.UPLOAD_DOCUMENT_JSON;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.UPLOADER_USERNAME;

@WithMockCatalogueUser
@Slf4j
@ActiveProfiles({"test", "upload:hubbub"})
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@DisplayName("HubbubUploadController")
@WebMvcTest(
    controllers = UploadController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class UploadControllerTest {

    @MockBean private UploadDocumentService uploadDocumentService;
    @MockBean private DocumentRepository documentRepository;
    @MockBean private JiraService jiraService;
    @MockBean(name="permission") private PermissionService permissionService;
    @MockBean private CatalogueService catalogueService;

    @Autowired private MockMvc mvc;
    @Autowired private Configuration configuration;

    private final String id = "164ef14f-95a5-45c7-8f36-d2000ba45516";

    private void givenUserCanUpload() {
        given(permissionService.userCanUpload(id)).willReturn(true);
    }

    @SneakyThrows
    private void givenGeminiDocument() {
        val gemini = new GeminiDocument();
        gemini.setTitle("Foo");
        gemini.setId(id);
        given(documentRepository.read(id))
            .willReturn(gemini);
    }

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("jira", jiraService);
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("permission", permissionService);
    }

    private void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue())
            .willReturn(Catalogue.builder()
                .title("Foo")
                .id("eidc")
                .url("bar")
                .build()
            );
    }

    private void givenUploadDocument() {
        val dropboxResponse = new HubbubResponse(new ArrayList<>(), new HubbubResponse.Pagination(1, 10, 20));
        val datastoreResponse = new HubbubResponse(new ArrayList<>(), new HubbubResponse.Pagination(1, 10, 100));
        val supportingDocumentsResponse = new HubbubResponse(new ArrayList<>(), new HubbubResponse.Pagination(1, 10, 1));
        val uploadDoc = new UploadDocument(id, dropboxResponse, datastoreResponse, supportingDocumentsResponse);
        given(uploadDocumentService.get(id, 1, 1, 1))
            .willReturn(uploadDoc);
    }

    @Test
    @SneakyThrows
    void getUploadPage() {
        //given
        givenUserCanUpload();
        givenGeminiDocument();
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();

        //when
        mvc.perform(
            get("/upload/{id}", id)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andExpect(view().name("html/upload/hubbub/upload-document"))
            .andExpect(model().attribute("id", id))
            .andExpect(model().attribute("title", "Foo"));
    }

    @Test
    @SneakyThrows
    void getUploadDocument() {
        //given
        givenUploadDocument();

        //when
        mvc.perform(
            get("/documents/{id}", id)
                .accept(UPLOAD_DOCUMENT_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(UPLOAD_DOCUMENT_JSON));

    }

    @Test
    @SneakyThrows
    void uploadFile() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(
            multipart("/documents/{id}/add-upload-document", id)
                .file(new MockMultipartFile(
                    "file",
                    "foo.txt",
                    "text/csv",
                    "some data".getBytes(StandardCharsets.UTF_8)
                ))
                .header("remote-user", UPLOADER_USERNAME)
        )
            .andExpect(status().isOk());

        //then
        verify(uploadDocumentService).add(eq(id), eq("foo.txt"), any(MultipartFile.class));
    }

    //TODO: test the rest of the Hubbub upload endpoints

}
