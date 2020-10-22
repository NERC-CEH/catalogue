package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
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
                get("/upload/993c5778-e139-4171-a57f-7a0f396be4b8")
                    .header("remote-user", "uploader")
        )
        .andExpect(status().isOk())
        .andExpect(model().attribute("id", "993c5778-e139-4171-a57f-7a0f396be4b8"))
        .andExpect(model().attributeExists("files", "title"))
        .andExpect(flash().attributeCount(0))
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        //then
        verify(storageService).loadAll("993c5778-e139-4171-a57f-7a0f396be4b8");
    }

    @Test
    @SneakyThrows
    public void adminCanAccessPage() {
        mockMvc.perform(
                get("/upload/993c5778-e139-4171-a57f-7a0f396be4b8")
                    .header("remote-user", "admin")
        )
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        //then
        verify(storageService).loadAll("993c5778-e139-4171-a57f-7a0f396be4b8");
    }

    @Test
    @SneakyThrows
    public void unprivilegedUserCanNotAccessPage() {
        mockMvc.perform(
                get("/upload/993c5778-e139-4171-a57f-7a0f396be4b8")
                        .header("remote-user", "unprivileged")
        )
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        //then
        verifyZeroInteractions(storageService);
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
    @SneakyThrows
    public void uploaderCanUploadFile() {
        val multipartFile = dataCsv();
        mockMvc.perform(
                fileUpload("/documents/993c5778-e139-4171-a57f-7a0f396be4b8/add-upload-document")
                        .file(multipartFile)
                        .header("remote-user", "uploader")
        )
        .andExpect(status().isFound())
        .andExpect(model().attributeExists("id"))
        .andExpect(flash().attributeExists("message"))
        .andExpect(header().string("Location", "/upload/993c5778-e139-4171-a57f-7a0f396be4b8"));

        //then
        verify(storageService).store("993c5778-e139-4171-a57f-7a0f396be4b8", multipartFile);
    }

    @Test
    @SneakyThrows
    public void unprivilegedUserCanNotUploadFile() {
        mockMvc.perform(
                fileUpload("/documents/993c5778-e139-4171-a57f-7a0f396be4b8/add-upload-document")
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
        mockMvc.perform(
                delete("/documents/993c5778-e139-4171-a57f-7a0f396be4b8/delete-upload-file/test.csv")
                        .header("remote-user", "uploader")
        )
        .andExpect(status().isFound())
        .andExpect(model().attributeExists("id"))
        .andExpect(flash().attributeExists("message"))
        .andExpect(header().string("Location", "/upload/993c5778-e139-4171-a57f-7a0f396be4b8"));

        //then
        verify(storageService).delete("993c5778-e139-4171-a57f-7a0f396be4b8", "test.csv");
    }

    @Test
    @SneakyThrows
    public void unprivilegedUserCanNotDeleteFile() {
        mockMvc.perform(
                delete("/documents/993c5778-e139-4171-a57f-7a0f396be4b8/delete-upload-file/test.csv")
                        .header("remote-user", "unprivileged")
        )
                .andExpect(status().isForbidden());

        //then
        verifyZeroInteractions(storageService);
    }

    // Needed as @MockBean only available in Spring Boot
    public static class TestConfig {
        @Bean
        public StorageService storageService() {
            return mock(StorageService.class);
        }
    }
}