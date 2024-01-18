package uk.ac.ceh.gateway.catalogue.upload.simple;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
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
import org.springframework.util.StreamUtils;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.UNPRIVILEGED_USERNAME;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.UPLOADER_USERNAME;

@WithMockCatalogueUser
@Slf4j
@ActiveProfiles({"test", "upload:simple"})
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class
})
@DisplayName("Simple Upload Controller")
@WebMvcTest(
    controllers = UploadController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class UploadControllerTest {
    @MockBean private StorageService storageService;
    @MockBean private DocumentRepository documentRepository;
    @MockBean private CatalogueService catalogueService;
    // Needed for security preauthorise method decisions
    @MockBean(name="permission") private PermissionService permission;

    @Autowired private MockMvc mvc;
    @Autowired Configuration configuration;

    private static final String ID = "993c5778-e139-4171-a57f-7a0f396be4b8";
    private static final String TITLE = "Belowground carbon stock data in the Ankeniheny Zahamena forest corridor, Madagascar";
    private static final String catalogueKey = "eidc";

    @SneakyThrows
    public String expectedResponse(String filename) {
        return StreamUtils.copyToString(
            getClass().getResourceAsStream(filename),
            StandardCharsets.UTF_8
        );
    }

    private void givenUserHasPermissionToUpload() {
        given(permission.userCanUpload(ID)).willReturn(true);
    }

    private void givenUserDoesNotHavePermissionToUpload() {
        given(permission.userCanUpload(ID)).willReturn(false);
    }

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("catalogues", catalogueService);
    }

    private void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue())
            .willReturn(
                Catalogue.builder()
                    .id("default")
                    .title("test")
                    .url("http://example.com")
                    .contactUrl("")
                    .logo("eidc.png")
                    .build()
            );
    }

    private void givenCatalogue() {
        given(catalogueService.retrieve(catalogueKey))
            .willReturn(
                Catalogue.builder()
                    .id(catalogueKey)
                    .title("Env Data Centre")
                    .url("https://example.com")
                    .contactUrl("")
                    .logo("eidc.png")
                    .build()
            );
    }
    private final FileInfo data1 = new FileInfo("data1.csv");
    private final FileInfo data2 = new FileInfo("data2.csv");
    private final FileInfo oneWithSpaces = new FileInfo("one with spaces.txt");
    private final List<FileInfo> fileInfos = Arrays.asList(
        data1,
        data2,
        oneWithSpaces
    );

    @Test
    @DisplayName("uploader can access page")
    @SneakyThrows
    void uploaderCanAccessPage() {
        //given
        val doc = new GeminiDocument();
        doc.setTitle(TITLE);
        doc.setMetadata(MetadataInfo.builder().catalogue(catalogueKey).build());

        givenUserHasPermissionToUpload();
        givenCatalogue();
        givenFreemarkerConfiguration();
        given(storageService.filenames(ID)).willReturn(fileInfos);
        given(documentRepository.read(ID)).willReturn(doc);

        //when
        mvc.perform(
            get("/upload/{id}", ID)
                .header("remote-user", UPLOADER_USERNAME)
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(view().name("html/upload/simple/upload"))
            .andExpect(model().attribute("id", ID))
            .andExpect(model().attribute("title", TITLE))
            .andExpect(model().attribute("catalogueKey", catalogueKey))
            .andExpect(model().attribute("files", hasItems(data1, data2, oneWithSpaces)))
            .andExpect(model().attributeDoesNotExist("message"));
    }

    @Test
    @DisplayName("non-uploader can not access page")
    @SneakyThrows
    void unprivilegedUserCanNotAccessPage() {
        //given
        givenUserDoesNotHavePermissionToUpload();
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();

        //when
        mvc.perform(
            get("/upload/{id}", ID)
                .header("remote-user", UNPRIVILEGED_USERNAME)
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isForbidden())
            .andExpect(view().name("html/access-denied"));

        //then
        verifyNoInteractions(documentRepository, storageService);
    }

    @Test
    @DisplayName("unauthenticated user can not access page")
    @SneakyThrows
    void unauthenticatedUserCanNotAccessPage() {
        //given
        givenUserDoesNotHavePermissionToUpload();
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();

        //when
        mvc.perform(
            get("/upload/{id}", ID)
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isForbidden())
            .andExpect(view().name("html/access-denied"));

        //then
        verifyNoInteractions(documentRepository, storageService);
    }

    @Test
    @DisplayName("Error message displayed on page")
    @SneakyThrows
    void errorDisplayingFilesOnPage() {
        //given
        val expectedMessage = new UploadController.ErrorMessage(
            "Failed to retrieve file information for 993c5778-e139-4171-a57f-7a0f396be4b8"
        );
        val doc = new GeminiDocument();
        doc.setTitle(TITLE);
        doc.setMetadata(MetadataInfo.builder().catalogue(catalogueKey).build());
        given(documentRepository.read(ID)).willReturn(doc);
        doThrow(new StorageServiceException(ID, "Failed to retrieve file information")).when(storageService).filenames(ID);
        givenUserHasPermissionToUpload();
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();

        //when
        mvc.perform(
            get("/upload/{id}", ID)
                .header("remote-user", UPLOADER_USERNAME)
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("id", ID))
            .andExpect(model().attribute("title", TITLE))
            .andExpect(model().attribute("catalogueKey", catalogueKey))
            .andExpect(model().attribute("files", emptyCollectionOf(String.class)))
            .andExpect(model().attribute("message", equalTo(expectedMessage)));
    }

    @Test
    @DisplayName("uploader can get filenames")
    @SneakyThrows
    void uploaderCanGetListOfFilenames() {
        //given
        val filenames = Arrays.asList(
            new FileInfo("data1.csv"),
            new FileInfo("data2.csv"),
            new FileInfo("name with spaces.csv")
        );
        givenUserHasPermissionToUpload();
        given(storageService.filenames(ID)).willReturn(filenames);

        mvc.perform(
            get("/upload/{id}", ID)
                .header("remote-user", UPLOADER_USERNAME)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponse("successfulFilenames.json")));

    }

    @Test
    @DisplayName("unprivileged user cannot get filenames")
    @SneakyThrows
    void unprivilegedUserCanNotGetListOfFilenames() {
        //given
        givenUserDoesNotHavePermissionToUpload();
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();

        //when
        mvc.perform(
            get("/upload/{id}", ID)
                .header("remote-user", UNPRIVILEGED_USERNAME)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isForbidden())
            .andExpect(view().name("html/access-denied"));

        verifyNoInteractions(storageService);
    }

    @Test
    @DisplayName("unauthenticated user cannot get filename")
    @SneakyThrows
    public void unauthenticatedUserCanNotGetListOfFilenames() {
        //given
        givenUserDoesNotHavePermissionToUpload();
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();

        //when
        mvc.perform(
            get("/upload/{id}", ID)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isForbidden())
            .andExpect(view().name("html/access-denied"));

        verifyNoInteractions(storageService);
    }

    @Test
    @DisplayName("user tries to retrieve filenames for unknown file id")
    @SneakyThrows
    public void unknownFilenames() {
        //given
        givenUserHasPermissionToUpload();
        doThrow(new UserInputException(ID, "Could not retrieve files")).when(storageService).filenames(ID);

        mvc.perform(
            get("/upload/{id}", ID)
                .header("remote-user", UPLOADER_USERNAME)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponse("unknownFilenames.json")));
    }

    @Test
    @DisplayName("storage service throws an exception")
    @SneakyThrows
    public void errorMessageGettingFilenames() {
        //given
        givenUserHasPermissionToUpload();
        doThrow(new StorageServiceException(ID, "Could not retrieve files")).when(storageService).filenames(ID);

        mvc.perform(
            get("/upload/{id}", ID)
                .header("remote-user", UPLOADER_USERNAME)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponse("errorFilenames.json")));
    }

    @SneakyThrows
    private MockMultipartFile fileWithSpacesCsv() {
        return new MockMultipartFile(
            "file",
            "file with spaces.csv",
            "text/csv",
            IOUtils.toByteArray(getClass().getResourceAsStream("file with spaces.csv"))
        );
    }

    @SneakyThrows
    private MockMultipartFile dataCsv() {
        return new MockMultipartFile(
            "file",
            "data.csv",
            "text/csv",
            IOUtils.toByteArray(getClass().getResourceAsStream("data.csv"))
        );
    }

    @Test
    @DisplayName("uploader can upload file")
    @SneakyThrows
    void uploaderCanUploadFile() {
        //given
        givenUserHasPermissionToUpload();
        MockMultipartFile multipartFile = dataCsv();

        //when
        mvc.perform(
            multipart("/upload/{id}", ID)
                .file(multipartFile)
                .header("remote-user", UPLOADER_USERNAME)
        )
            .andExpect(status().isNoContent());

        //then
        verify(storageService).store(ID, multipartFile);
    }

    @Test
    @DisplayName("unprivileged user can not upload file")
    @SneakyThrows
    void unprivilegedUserCanNotUploadFile() {
        //given
        givenUserDoesNotHavePermissionToUpload();
        MockMultipartFile multipartFile = dataCsv();
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();

        //when
        mvc.perform(
            multipart("/upload/{id}", ID)
                .file(multipartFile)
                .header("remote-user", UNPRIVILEGED_USERNAME)
        )
            .andExpect(status().isForbidden())
            .andExpect(view().name("html/access-denied"));

        //then
        verifyNoInteractions(storageService);
    }

    @Test
    @DisplayName("error when file uploaded with same name as existing file")
    @SneakyThrows
    void errorWhenFileUploadedWithSameNameAsExistingFile() {
        //given
        givenUserHasPermissionToUpload();
        MockMultipartFile multipartFile = dataCsv();
        doThrow(new FileExitsException(ID, multipartFile.getOriginalFilename()))
            .when(storageService)
            .store(ID, multipartFile);

        //when
        mvc.perform(
            multipart("http://example.com/upload/{id}", ID)
                .file(multipartFile)
                .header("remote-user", UPLOADER_USERNAME)
        )
            .andExpect(status().isConflict())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponse("errorFileExistsUpload.json")));
    }

    @Test
    @DisplayName("StorageService cannot save file and throws an error")
    @SneakyThrows
    public void errorSavingFile() {
        //given
        givenUserHasPermissionToUpload();
        MockMultipartFile multipartFile = dataCsv();
        doThrow(new StorageServiceException(ID, "Could not upload data.csv"))
            .when(storageService)
            .store(ID, multipartFile);

        //when
        mvc.perform(
            multipart("http://example.com/upload/{id}", ID)
                .file(multipartFile)
                .header("remote-user", UPLOADER_USERNAME)
        )
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponse("errorUpload.json")));

    }

    @Test
    @DisplayName("user can upload file with spaces in name")
    @SneakyThrows
    void uploaderCanUploadFileWithSpaces() {
        //given
        givenUserHasPermissionToUpload();
        val fileWithSpaces = fileWithSpacesCsv();

        //when
        mvc.perform(
            multipart("http://example.com/upload/{id}", ID)
                .file(fileWithSpaces)
                .header("remote-user", UPLOADER_USERNAME)
        )
            .andExpect(status().isNoContent());

        //then
        verify(storageService).store(ID, fileWithSpaces);
    }

    private final String filename = "test.csv";

    @Test
    @SneakyThrows
    @DisplayName("uploader is allowed to delete file")
    void uploaderCanDeleteFile() {
        //given
        givenUserHasPermissionToUpload();

        //when
        mvc.perform(
            delete("/upload/{id}/{filename}", ID, filename)
                .header("remote-user", UPLOADER_USERNAME)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNoContent());

        //then
        verify(storageService).delete(ID, filename);
    }

    @Test
    @SneakyThrows
    @DisplayName("user without upload permissions cannot delete file")
    void unprivilegedUserCanNotDeleteFile() {
        //given
        givenUserDoesNotHavePermissionToUpload();
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();

        //when
        mvc.perform(
            delete("/upload/{id}/{filename}", ID, filename)
                .header("remote-user", UNPRIVILEGED_USERNAME)
        )
            .andExpect(status().isForbidden())
            .andExpect(view().name("html/access-denied"));

        //then
        verifyNoInteractions(storageService);
    }

    @Test
    @DisplayName("error deleting file")
    @SneakyThrows
    void errorDeletingFile() {
        //given
        givenUserHasPermissionToUpload();
        doThrow(new StorageServiceException(ID, "Error trying to delete test.csv"))
            .when(storageService)
            .delete(ID, filename);

        //when
        mvc.perform(
            delete("/upload/{id}/{filename}", ID, filename)
                .header("remote-user", UPLOADER_USERNAME)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponse("errorDeletion.json")));
    }

    @Test
    @SneakyThrows
    void deletingUnknownFile() {
        //given
        givenUserHasPermissionToUpload();
        doThrow(new UserInputException(ID, "File not found test.csv"))
            .when(storageService)
            .delete(ID, filename);

        //when
        mvc.perform(
            delete("/upload/{id}/{filename}", ID, filename)
                .header("remote-user", UPLOADER_USERNAME)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponse("notFoundDeletion.json")));
    }

    @Test
    @DisplayName("delete file with spaces in name")
    @SneakyThrows
    void canDeleteFileWithSpacesInName() {
        //given
        givenUserHasPermissionToUpload();
        val filenameWithSpaces = "data with spaces.csv";

        //when
        mvc.perform(
            delete("/upload/{id}/{filename}", ID, filenameWithSpaces)
                .header("remote-user", UPLOADER_USERNAME)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNoContent());

        //then
        verify(storageService).delete(ID, filenameWithSpaces);
    }
}
