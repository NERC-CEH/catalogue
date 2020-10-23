package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ceh.gateway.catalogue.config.WebConfig;
import uk.ac.ceh.gateway.catalogue.upload.simple.UploadController.FileInfo;

import java.nio.file.FileAlreadyExistsException;

import static java.lang.String.format;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ActiveProfiles({"development", "upload:simple"})
@TestPropertySource
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfig.class, UploadControllerTest.TestConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
// DirtiesContext needed as StorageService is a Mock that needs refreshing before each test
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UploadControllerTest {
    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;
    @Autowired
    private StorageService storageService;

    private final String id = "993c5778-e139-4171-a57f-7a0f396be4b8";
    private final String title = "Belowground carbon stock data in the Ankeniheny Zahamena forest corridor, Madagascar";

    // Needed as @MockBean only available in Spring Boot
    public static class TestConfig {
        @Bean
        public StorageService storageService() {
            return mock(StorageService.class);
        }
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

    @Before
    public void setup() {
        mockMvc = webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    @Test
    @SneakyThrows
    public void uploaderCanAccessPage() {
        mockMvc.perform(
                get("/upload/{id}", id)
                    .header("remote-user", "uploader")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("id", id))
                .andExpect(model().attribute("title", title))
                .andExpect(model().attributeExists("files"))
                .andExpect(flash().attributeCount(0))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        //then
        verify(storageService).filenames(id);
    }

    @Test
    @SneakyThrows
    public void adminCanAccessPage() {
        mockMvc.perform(
                get("/upload/{id}", id)
                    .header("remote-user", "admin")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("id", id))
                .andExpect(model().attribute("title", title))
                .andExpect(model().attributeExists("files"))
                .andExpect(flash().attributeCount(0))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        //then
        verify(storageService).filenames(id);
    }

    @Test
    @SneakyThrows
    public void unprivilegedUserCanNotAccessPage() {
        mockMvc.perform(
                get("/upload/{id}", id)
                        .header("remote-user", "unprivileged")
        )
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        //then
        verifyZeroInteractions(storageService);
    }

    @Test
    @SneakyThrows
    public void unauthenticatedUserCanNotAccessPage() {
        mockMvc.perform(
                get("/upload/{id}", id)
        )
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        //then
        verifyZeroInteractions(storageService);
    }

    @Test
    @SneakyThrows
    public void errorWhenFileUploadedWithSameNameAsExistingFile() {
        //given
        val multipartFile = dataCsv();
        doThrow(new FileAlreadyExistsException("data.csv")).when(storageService).store(id, multipartFile);

        //when
        mockMvc.perform(
                fileUpload("/documents/{id}/add-upload-document", id)
                        .file(multipartFile)
                        .header("remote-user", "uploader")
        )
                .andExpect(status().isFound())
                .andExpect(model().attribute("id", id))
                .andExpect(flash().attribute("error", "Cannot upload data.csv, file already exists"))
                .andExpect(header().string("Location", format("/upload/%s", id)));

        //then
        verify(storageService).store(id, multipartFile);
    }

    @Ignore
    @Test
    public void errorDeletingFile() {
        fail();
    }

    @Test
    @SneakyThrows
    public void errorDisplayingFilesOnPage() {
        //given
        doThrow(new RuntimeException()).when(storageService).filenames(id);

        //when
        mockMvc.perform(
                get("/upload/{id}", id)
                        .header("remote-user", "uploader")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("id", id))
                .andExpect(model().attribute("title", title))
                .andExpect(model().attribute("files", emptyCollectionOf(FileInfo.class)))
                .andExpect(model().attribute("error", "Failed to retrieve information"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        //then
        verify(storageService).filenames(id);
    }

    @Ignore
    @Test
    public void canDeleteFileWithSpacesInName() {
        fail();
    }

    @Ignore
    @Test
    public void errorSavingFile() {
        fail();
    }

    @Test
    @SneakyThrows
    public void uploaderCanUploadFile() {
        //given
        val multipartFile = dataCsv();

        //when
        mockMvc.perform(
                fileUpload("/documents/{id}/add-upload-document", id)
                        .file(multipartFile)
                        .header("remote-user", "uploader")
        )
        .andExpect(status().isFound())
        .andExpect(model().attribute("id", id))
        .andExpect(flash().attribute("message", "Successfully uploaded data.csv"))
        .andExpect(header().string("Location", format("/upload/%s", id)));

        //then
        verify(storageService).store(id, multipartFile);
    }

    @Test
    @SneakyThrows
    public void unprivilegedUserCanNotUploadFile() {
        mockMvc.perform(
                fileUpload("/documents/{id}/add-upload-document", id)
                        .file(dataCsv())
                        .header("remote-user", "unprivileged")
        )
                .andExpect(status().isForbidden());

        //then
        verifyZeroInteractions(storageService);
    }

    @Test
    @SneakyThrows
    public void uploaderCanDeleteFile() {
        //given
        val filename = "test.csv";

        //when
        mockMvc.perform(
                delete("/documents/{id}/delete-upload-file/{filename}", id, filename)
                        .header("remote-user", "uploader")
        )
        .andExpect(status().isFound())
        .andExpect(model().attribute("id", id))
        .andExpect(flash().attribute("message", format("Successfully deleted %s", filename)))
        .andExpect(header().string("Location", format("/upload/%s", id)));

        //then
        verify(storageService).delete(id, "test.csv");
    }

    @Test
    @SneakyThrows
    public void unprivilegedUserCanNotDeleteFile() {
        mockMvc.perform(
                delete("/documents/{id}/delete-upload-file/test.csv", id)
                        .header("remote-user", "unprivileged")
        )
                .andExpect(status().isForbidden());

        //then
        verifyZeroInteractions(storageService);
    }
}