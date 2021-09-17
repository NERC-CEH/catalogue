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
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.TEXT_CSV;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.UPLOADER_USERNAME;
import static uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadController.*;

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

    @MockBean private UploadService uploadService;
    @MockBean private DocumentRepository documentRepository;
    @MockBean private JiraService jiraService;
    @MockBean(name="permission") private PermissionService permissionService;
    @MockBean private CatalogueService catalogueService;

    @Autowired private MockMvc mvc;
    @Autowired private Configuration configuration;

    private final String id = "164ef14f-95a5-45c7-8f36-d2000ba45516";
    private final String path = "/dropbox/" + id + "/dataset.csv";
    private final String jiraKey = "TEST-1";

    private void givenUserCanUpload() {
        given(permissionService.userCanUpload(id)).willReturn(true);
    }

    private void givenUserCanAccess() {
        given(permissionService.toAccess(any(CatalogueUser.class), eq(id), eq("VIEW")))
            .willReturn(true);
    }

    private void givenUserIsAdmin() {
        given(permissionService.userIsAdmin()).willReturn(true);
    }

    private void givenUserIsNonAdmin() {
        given(permissionService.userIsAdmin()).willReturn(false);
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
        configuration.setSharedVariable("catalogues", catalogueService);
    }

    private void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue())
            .willReturn(Catalogue.builder()
                .title("Foo")
                .id("eidc")
                .url("bar")
                .contactUrl("")
                .build()
            );
    }

    private void givenDataTransferIssue() {
        val status = new HashMap<String, Object>();
        status.put("name", "Scheduled");
        val fields = new HashMap<String, Object>();
        fields.put("status", status);
        val dataTransfer = new JiraIssue();
        dataTransfer.setFields(fields);
        dataTransfer.setKey(jiraKey);
        given(jiraService.retrieveDataTransferIssue(id))
            .willReturn(Optional.of(dataTransfer));
    }

    private void givenClosedDataTransferIssue() {
        val status = new HashMap<String, Object>();
        status.put("name", "Closed");
        val fields = new HashMap<String, Object>();
        fields.put("status", status);
        val dataTransfer = new JiraIssue();
        dataTransfer.setFields(fields);
        dataTransfer.setKey(jiraKey);
        given(jiraService.retrieveDataTransferIssue(id))
            .willReturn(Optional.of(dataTransfer));
    }

    @SneakyThrows
    private void givenMetadataDocument() {
        val info = MetadataInfo.builder().build();
        val metadata = new GeminiDocument();
        metadata.setMetadata(info);
        given(documentRepository.read(id))
            .willReturn(metadata);
    }

    private void givenDatastore() {
        val data = Collections.singletonList(
            new FileInfo(23L, "hash", "name", "/eidchub/path", "status", 234234234L)
        );
        given(uploadService.get(id, "dropbox", 1, 20))
            .willReturn(data);
    }

    private void givenDropbox() {
        val data = Collections.singletonList(
            new FileInfo(23L, "hash", "name", "/dropbox/path", "status", 234234234L)
        );
        given(uploadService.get(id, "dropbox", 1, 20))
            .willReturn(data);
    }

    private void givenMetadata() {
        val data = Collections.singletonList(
            new FileInfo(23L, "hash", "name", "/supporting-documents/path", "status", 234234234L)
        );
        given(uploadService.get(id, "dropbox", 1, 20))
            .willReturn(data);
    }

    private void givenFileInfo() {
        val fileInfo = new FileInfo(23L, "hash", "name", "path", "status", 234234234L);
        given(uploadService.get(id, path))
            .willReturn(fileInfo);
    }

    @Test
    @SneakyThrows
    void getScheduledPageForAdmin() {
        //given
        givenUserCanUpload();
        givenUserIsAdmin();
        givenGeminiDocument();
        givenDataTransferIssue();
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();
        givenDatastore();
        givenDropbox();
        givenMetadata();

        //when
        mvc.perform(
            get("/upload/{id}", id)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andExpect(view().name("html/upload/hubbub/upload"))
            .andExpect(model().attribute("id", id))
            .andExpect(model().attribute("title", "Foo"))
            .andExpect(model().attribute("isAdmin", true))
            .andExpect(model().attribute("isOpen", false))
            .andExpect(model().attribute("isScheduled", true))
            .andExpect(model().attribute("isInProgress", false))
            .andExpect(model().attributeExists("datastore", "dropbox", "metadata", "maxFileSize"));

    }

    @Test
    @SneakyThrows
    void getClosedPageForAdmin() {
        //given
        givenUserCanUpload();
        givenUserIsAdmin();
        givenGeminiDocument();
        givenClosedDataTransferIssue();
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();
        givenDatastore();
        givenDropbox();
        givenMetadata();

        //when
        mvc.perform(
                get("/upload/{id}", id)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andExpect(view().name("html/upload/hubbub/upload"))
            .andExpect(model().attribute("id", id))
            .andExpect(model().attribute("title", "Foo"))
            .andExpect(model().attribute("isAdmin", true))
            .andExpect(model().attribute("isOpen", false))
            .andExpect(model().attribute("isScheduled", false))
            .andExpect(model().attribute("isInProgress", false))
            .andExpect(model().attributeExists("datastore", "dropbox", "metadata", "maxFileSize"));

    }

    @Test
    @SneakyThrows
    void getScheduledPageForNonAdmin() {
        //given
        givenUserCanUpload();
        givenUserIsNonAdmin();
        givenGeminiDocument();
        givenDataTransferIssue();
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();
        givenDropbox();

        //when
        mvc.perform(
                get("/upload/{id}", id)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andExpect(view().name("html/upload/hubbub/upload"))
            .andExpect(model().attribute("id", id))
            .andExpect(model().attribute("title", "Foo"))
            .andExpect(model().attribute("isAdmin", false))
            .andExpect(model().attribute("isOpen", false))
            .andExpect(model().attribute("isScheduled", true))
            .andExpect(model().attribute("isInProgress", false))
            .andExpect(model().attributeExists("dropbox", "maxFileSize"))
            .andExpect(model().attributeDoesNotExist("datastore", "metadata"));

    }

    @Test
    @SneakyThrows
    void getDropboxFileInfos() {
        //given
        givenUserCanAccess();
        givenDropbox();

        //when
        mvc.perform(
            get("/upload/{id}/dropbox", id)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON));

    }

    @Test
    @SneakyThrows
    void getIndividualFileInfo() {
        //given
        givenUserCanAccess();
        givenFileInfo();

        //when
        mvc.perform(
            get("/upload/{id}", id)
            .queryParam("path", path)
            .accept(APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    @SneakyThrows
    void upload() {
        //given
        givenUserCanUpload();
        val multipartFile = new MockMultipartFile(
            "file",
            "foo.txt",
            "text/csv",
            "some data".getBytes(UTF_8)
        );

        //when
        mvc.perform(
            multipart("/upload/{id}", id)
                .file(multipartFile)
                .header("remote-user", UPLOADER_USERNAME)
        )
            .andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).upload(id, multipartFile);
    }

    @Test
    @SneakyThrows
    void accept() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(post("/upload/{id}/accept", id)
            .queryParam("path", path)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).accept(path);
    }

    @Test
    @SneakyThrows
    void cancel() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(post("/upload/{id}/cancel", id)
            .queryParam("path", path)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).cancel(path);
    }

    @Test
    @SneakyThrows
    void csvWithAcceptHeader() {
        //given
        givenUserCanAccess();

        //when
        mvc.perform(get("/upload/{id}", id)
            .accept(TEXT_CSV)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).csv(any(PrintWriter.class), eq(id));
    }

    @Test
    @SneakyThrows
    void csvWithFormatParameter() {
        //given
        givenUserCanAccess();

        //when
        mvc.perform(get("/upload/{id}", id)
            .queryParam("format", "csv")
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).csv(any(PrintWriter.class), eq(id));
    }

    @Test
    @SneakyThrows
    void deleteFile() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(delete("/upload/{id}", id)
            .queryParam("path", path)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).delete(path);
    }

    @Test
    @SneakyThrows
    void finish() {
        //given
        givenUserCanUpload();
        givenDataTransferIssue();
        givenMetadataDocument();

        //when
        mvc.perform(post("/upload/{id}/finish", id)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(jiraService).transition(jiraKey, START_PROGRESS);
        verify(jiraService).comment(eq(jiraKey), any(String.class));
        verify(documentRepository)
            .save(any(CatalogueUser.class), any(MetadataDocument.class), eq(id), any(String.class));
    }

    @Test
    @SneakyThrows
    void moveDatastore() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(post("/upload/{id}/move-datastore", id)
            .queryParam("path", path)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).move(path, DATASTORE);
    }

    @Test
    @SneakyThrows
    void moveMetadata() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(post("/upload/{id}/move-metadata", id)
            .queryParam("path", path)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).move(path, METADATA);
    }

    @Test
    @SneakyThrows
    void moveAllToDatastore() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(post("/upload/{id}/move-all-datastore", id)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).moveAllToDataStore(id);
    }

    @Test
    @SneakyThrows
    void reschedule() {
        //given
        givenUserCanUpload();
        givenDataTransferIssue();

        //when
        mvc.perform(post("/upload/{id}/reschedule", id)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(jiraService).transition(jiraKey, HOLD);
        verify(jiraService).transition(jiraKey, UNHOLD);
        verify(jiraService).transition(jiraKey, APPROVE);
        verify(jiraService).transition(jiraKey, SCHEDULED);
        verify(jiraService).comment(eq(jiraKey), any(String.class));
    }

    @Test
    @SneakyThrows
    void schedule() {
        //given
        givenUserCanUpload();
        givenDataTransferIssue();

        //when
        mvc.perform(post("/upload/{id}/schedule", id)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(jiraService).transition(jiraKey, APPROVE);
        verify(jiraService).transition(jiraKey, SCHEDULED);
        verify(jiraService).comment(eq(jiraKey), any(String.class));
    }

    @Test
    @SneakyThrows
    void validateWithPathParameter() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(post("/upload/{id}/validate", id)
            .queryParam("path", path)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).validate(path);
    }

    @Test
    @SneakyThrows
    void validateWithoutPathParameter() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(post("/upload/{id}/validate", id)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).validate(id);
    }
}
