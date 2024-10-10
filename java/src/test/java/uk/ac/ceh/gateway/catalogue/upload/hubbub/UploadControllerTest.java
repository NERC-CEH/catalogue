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
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.io.PrintWriter;
import java.time.LocalDateTime;
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

@WithMockCatalogueUser(username = UPLOADER_USERNAME)
@Slf4j
@ActiveProfiles({"test", "upload:hubbub"})
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class
})
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
    @MockBean private ProfileService profileService;

    @Autowired private MockMvc mvc;
    @Autowired private Configuration configuration;

    private final String datasetId = "164ef14f-95a5-45c7-8f36-d2000ba45516";
    private final String path = "dataset.csv";
    private final LocalDateTime lastModified = LocalDateTime.of(2022, 3, 12, 14, 26, 52);
    private final LocalDateTime lastValidated = LocalDateTime.of(2022, 3, 26, 23, 9, 12);
    private final String jiraKey = "TEST-1";

    private void givenUserCanUpload() {
        given(permissionService.userCanUpload(datasetId)).willReturn(true);
    }

    private void givenUserCanAccess() {
        given(permissionService.toAccess(any(CatalogueUser.class), eq(datasetId), eq("VIEW")))
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
        gemini.setId(datasetId);
        given(documentRepository.read(datasetId))
            .willReturn(gemini);
    }

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("profile", profileService);
    }

    private void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue())
            .willReturn(Catalogue.builder()
                .title("Foo")
                .id("eidc")
                .url("bar")
                .contactUrl("")
                .logo("eidc.png")
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
        given(jiraService.retrieveDataTransferIssue(datasetId))
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
        given(jiraService.retrieveDataTransferIssue(datasetId))
            .willReturn(Optional.of(dataTransfer));
    }

    @SneakyThrows
    private void givenMetadataDocument() {
        val info = MetadataInfo.builder().build();
        val metadata = new GeminiDocument();
        metadata.setMetadata(info);
        given(documentRepository.read(datasetId))
            .willReturn(metadata);
    }

    private void givenDatastore() {
        val data = Collections.singletonList(
            new HubbubResponse.FileInfo(
                456L,
                datasetId,
                DATASTORE,
                "csv",
                "fc3facd3122cb0250f4bf82746d4bd13",
                0.32,
                lastModified,
                lastValidated,
                path,
                "VALID",
                "text/csv"
            )
        );
        given(uploadService.get(datasetId, DATASTORE, 1, 20))
            .willReturn(new HubbubResponse(data, null, null));
    }

    private void givenIndividualFile() {
        val data = Collections.singletonList(
            new HubbubResponse.FileInfo(
                456L,
                datasetId,
                DROPBOX,
                "csv",
                "fc3facd3122cb0250f4bf82746d4bd13",
                0.32,
                lastModified,
                lastValidated,
                path,
                "VALID",
                "text/csv"
            )
        );
        given(uploadService.get(datasetId, DROPBOX, path))
            .willReturn(new HubbubResponse(data, null, null));
    }

    private void givenDropbox() {
        val data = Collections.singletonList(
            new HubbubResponse.FileInfo(
                456L,
                datasetId,
                DROPBOX,
                "csv",
                "fc3facd3122cb0250f4bf82746d4bd13",
                0.32,
                lastModified,
                lastValidated,
                path,
                "VALID",
                "text/csv"
            )
        );
        given(uploadService.get(datasetId, DROPBOX, 1, 20))
            .willReturn(new HubbubResponse(data, null, null));
    }

    private void givenMetadata() {
        val data = Collections.singletonList(
            new HubbubResponse.FileInfo(
                456L,
                datasetId,
                METADATA,
                "csv",
                "fc3facd3122cb0250f4bf82746d4bd13",
                0.32,
                lastModified,
                lastValidated,
                path,
                "VALID",
                "text/csv"
            )
        );
        given(uploadService.get(datasetId, METADATA, 1, 20))
            .willReturn(new HubbubResponse(data, null, null));
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
            get("/upload/{datasetId}", datasetId)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andExpect(view().name("html/upload/hubbub/upload"))
            .andExpect(model().attribute("id", datasetId))
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
                get("/upload/{datasetId}", datasetId)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andExpect(view().name("html/upload/hubbub/upload"))
            .andExpect(model().attribute("id", datasetId))
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
                get("/upload/{datasetId}", datasetId)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andExpect(view().name("html/upload/hubbub/upload"))
            .andExpect(model().attribute("id", datasetId))
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
            get("/upload/{datasetId}/dropbox", datasetId)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON));

    }

    @Test
    @SneakyThrows
    void getIndividualFile() {
        //given
        givenUserCanAccess();
        givenIndividualFile();

        //when
        mvc.perform(
                get("/upload/{datasetId}/dropbox?path={path}", datasetId, path)
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
            multipart("/upload/{datasetId}", datasetId)
                .file(multipartFile)
                .header("remote-user", UPLOADER_USERNAME)
        )
            .andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).upload(datasetId, UPLOADER_USERNAME, multipartFile);
    }

    @Test
    @SneakyThrows
    void accept() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(post("/upload/{datasetId}/{datastore}/accept", datasetId, DROPBOX)
            .queryParam("path", path)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).accept(datasetId, DROPBOX, path, UPLOADER_USERNAME);
    }

    @Test
    @SneakyThrows
    void cancel() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(post("/upload/{datasetId}/{datastore}/cancel", datasetId, DROPBOX)
            .queryParam("path", path)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).cancel(datasetId, DROPBOX, path, UPLOADER_USERNAME);
    }

    @Test
    @SneakyThrows
    void csvWithAcceptHeader() {
        //given
        givenUserCanAccess();

        //when
        mvc.perform(get("/upload/{datasetId}", datasetId)
            .accept(TEXT_CSV)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).csv(any(PrintWriter.class), eq(datasetId));
    }

    @Test
    @SneakyThrows
    void csvWithFormatParameter() {
        //given
        givenUserCanAccess();

        //when
        mvc.perform(get("/upload/{datasetId}", datasetId)
            .queryParam("format", "csv")
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).csv(any(PrintWriter.class), eq(datasetId));
    }

    @Test
    @SneakyThrows
    void deleteFile() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(delete("/upload/{datasetId}/{datastore}", datasetId, DROPBOX)
            .queryParam("path", path)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).delete(datasetId, DROPBOX, path, UPLOADER_USERNAME);
    }

    @Test
    @SneakyThrows
    void finish() {
        //given
        givenUserCanUpload();
        givenDataTransferIssue();
        givenMetadataDocument();

        //when
        mvc.perform(post("/upload/{datasetId}/finish", datasetId)
            .header("remote-user", UPLOADER_USERNAME)
        )
            .andExpect(status().is2xxSuccessful());

        //then
        verify(jiraService).transition(jiraKey, START_PROGRESS);
        verify(jiraService).comment(eq(jiraKey), any(String.class));
        verify(documentRepository)
            .save(any(CatalogueUser.class), any(MetadataDocument.class), eq(datasetId), any(String.class));
    }

    @Test
    @SneakyThrows
    void hashDropbox() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(post("/upload/{datasetId}/hash", datasetId)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).hashDropbox(datasetId, UPLOADER_USERNAME);
    }

    @Test
    @SneakyThrows
    void register() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(post("/upload/{datasetId}/register", datasetId)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).register(datasetId, UPLOADER_USERNAME);
    }

    @Test
    @SneakyThrows
    void unregister() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(post("/upload/{datasetId}/{datastore}/unregister", datasetId, DATASTORE)
            .queryParam("path", path)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).unregister(datasetId, DATASTORE, path, UPLOADER_USERNAME);
    }

    @Test
    @SneakyThrows
    void moveDatastore() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(post("/upload/{datasetId}/{datastore}/move", datasetId, DROPBOX)
            .queryParam("path", path)
            .queryParam("to", DATASTORE)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).move(datasetId, DROPBOX, Optional.of(path), UPLOADER_USERNAME, DATASTORE);
    }

    @Test
    @SneakyThrows
    void moveMetadata() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(post("/upload/{datasetId}/{datastore}/move", datasetId, DROPBOX)
            .queryParam("path", path)
            .queryParam("to", METADATA)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).move(datasetId, DROPBOX, Optional.of(path), UPLOADER_USERNAME, METADATA);
    }

    @Test
    @SneakyThrows
    void moveAllToDatastore() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(post("/upload/{datasetId}/{datastore}/move", datasetId, DROPBOX)
            .queryParam("to", DATASTORE)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).move(datasetId, DROPBOX, Optional.empty(), UPLOADER_USERNAME, DATASTORE);
    }

    @Test
    @SneakyThrows
    void reschedule() {
        //given
        givenUserCanUpload();
        givenDataTransferIssue();

        //when
        mvc.perform(post("/upload/{datasetId}/reschedule", datasetId)
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
        mvc.perform(post("/upload/{datasetId}/schedule", datasetId)
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
        mvc.perform(post("/upload/{datasetId}/{datastore}/validate", datasetId, DROPBOX)
            .queryParam("path", path)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).validate(datasetId, DROPBOX, Optional.of(path), UPLOADER_USERNAME);
    }

    @Test
    @SneakyThrows
    void validateWithoutPathParameter() {
        //given
        givenUserCanUpload();

        //when
        mvc.perform(post("/upload/{datasetId}/{datastore}/validate", datasetId, DATASTORE)
            .queryParam("username", UPLOADER_USERNAME)
            .header("remote-user", UPLOADER_USERNAME)
        ).andExpect(status().is2xxSuccessful());

        //then
        verify(uploadService).validate(datasetId, DATASTORE,Optional.empty(), UPLOADER_USERNAME);
    }
}
