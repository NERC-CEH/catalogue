package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.config.WebConfig;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadControllerUtils.ID;

@DisplayName("Simple Upload Controller")
@ActiveProfiles({"development", "upload:simple"})
@ContextConfiguration(classes = {
    UploadController.class,
    DevelopmentUserStoreConfig.class,
    SecurityConfigCrowd.class,
    SecurityConfig.class,
    WebConfig.class
})
@WebMvcTest(UploadController.class)
class UploadControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private StorageService storageService;
    @MockBean private DocumentRepository documentRepository;

    // Needed for security preauthorise method decisions
    @MockBean(name="permission") private PermissionService permission;

    private final String filename = "test.csv";

    @Nested
    @DisplayName("filenames")
    class Filenames {

        @Test
        @SneakyThrows
        @DisplayName("uploader can get filenames")
        void uploaderCanGetListOfFilenames() {
            //given
            val filenames = Arrays.asList(
                new FileInfo("data1.csv"),
                new FileInfo("data2.csv"),
                new FileInfo("name with spaces.csv")
            );
            given(permission.userCanUpload(ID)).willReturn(true);
            given(storageService.filenames(ID)).willReturn(filenames);

            mockMvc.perform(
                get("/upload/{id}", ID)
                    .header("remote-user", "uploader")
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse("successfulFilenames.json")));

        }

        @Test
        @SneakyThrows
        @DisplayName("unprivileged user cannot get filenames")
        public void unprivilegedUserCanNotGetListOfFilenames() {
            //given
            given(permission.userCanUpload(ID)).willReturn(false);

            //when
            mockMvc.perform(
                get("/upload/{id}", ID)
                    .header("remote-user", "unprivileged")
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

            verifyNoInteractions(storageService);
        }

        @Test
        @SneakyThrows
        public void unauthenticatedUserCanNotGetListOfFilenames() {

            mockMvc.perform(
                get("/upload/{id}", ID)
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

            verifyNoInteractions(storageService);
        }

        @Test
        @SneakyThrows
        public void unknownFilenames() {
            //given
            doThrow(new UserInputException(ID, "Could not retrieve files")).when(storageService).filenames(ID);

            mockMvc.perform(
                get("/upload/{id}", ID)
                    .header("remote-user", "uploader")
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse("unknownFilenames.json")));
        }

        @Test
        @SneakyThrows
        public void errorMessageGettingFilenames() {
            //given
            doThrow(new StorageServiceException(ID, "Could not retrieve files")).when(storageService).filenames(ID);

            mockMvc.perform(
                get("/upload/{id}", ID)
                    .header("remote-user", "uploader")
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse("errorFilenames.json")));
        }

    }

    @Nested
    @DisplayName("delete file")
    class Delete {

        @Test
        @SneakyThrows
        @DisplayName("uploader is allowed to delete file")
        void uploaderCanDeleteFile() {
            //given
            given(permission.userCanUpload(ID)).willReturn(true);

            //when
            mockMvc.perform(
                delete("/upload/{id}/{filename}", ID, filename)
                    .header("remote-user", "uploader")
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNoContent());

            //then
            verify(storageService).delete(ID, filename);
        }

        @Test
        @SneakyThrows
        @DisplayName("user without upload permissions cannot delete file")
        public void unprivilegedUserCanNotDeleteFile() {
            //given
            given(permission.userCanUpload(ID)).willReturn(false);

            //when
            mockMvc.perform(
                delete("/upload/{id}/{filename}", ID, filename)
                    .header("remote-user", "unprivileged")
            )
                .andExpect(status().isForbidden());

            //then
            verifyNoInteractions(storageService);
        }

        @Test
        @SneakyThrows
        public void errorDeletingFile() {
            //given
            given(permission.userCanUpload(ID)).willReturn(true);
            doThrow(new StorageServiceException(ID, "Error trying to delete test.csv"))
                .when(storageService)
                .delete(ID, filename);

            //when
            mockMvc.perform(
                delete("/upload/{id}/{filename}", ID, filename)
                    .header("remote-user", "uploader")
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse("errorDeletion.json")));
        }

        @Test
        @SneakyThrows
        public void deletingUnknownFile() {
            //given
            doThrow(new UserInputException(ID, "File not found test.csv"))
                .when(storageService)
                .delete(ID, filename);

            //when
            mockMvc.perform(
                delete("/upload/{id}/{filename}", ID, filename)
                    .header("remote-user", "uploader")
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse("notFoundDeletion.json")));
        }

        @Test
        @SneakyThrows
        public void canDeleteFileWithSpacesInName() {
            //given
            val filenameWithSpaces = "data with spaces.csv";
            //when
            mockMvc.perform(
                delete("/upload/{id}/{filename}", ID, filenameWithSpaces)
                    .header("remote-user", "uploader")
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNoContent());

            //then
            verify(storageService).delete(ID, filenameWithSpaces);
        }
    }

    @SneakyThrows
    public String expectedResponse(String filename) {
        return StreamUtils.copyToString(
            getClass().getResourceAsStream(filename),
            StandardCharsets.UTF_8
        );
    }
}