package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ceh.gateway.catalogue.config.WebConfig;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadControllerUtils.*;

/**
 * Test Upload Controller file upload endpoint
 */
@Disabled
@ActiveProfiles({"development", "upload:simple"})
@TestPropertySource("UploadControllerTest.properties")
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfig.class, UploadControllerUtils.TestConfig.class})
@ExtendWith(SpringExtension.class)
// DirtiesContext needed as StorageService is a Mock that needs refreshing before each test
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UploadControllerUploadTest {
    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;
    @Autowired
    private StorageService storageService;

    @BeforeEach
    public void setup() {
        mockMvc = webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    @Test
    @SneakyThrows
    public void uploaderCanUploadFile() {
        //given
        MockMultipartFile multipartFile = dataCsv(getClass());

        //when
        mockMvc.perform(
                multipart("http://example.com/upload/{id}", ID)
                        .file(multipartFile)
                        .header("remote-user", "uploader")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNoContent());

        //then
        verify(storageService).store(ID, multipartFile);
    }

    @Test
    @SneakyThrows
    public void unprivilegedUserCanNotUploadFile() {
        //given
        MockMultipartFile multipartFile = dataCsv(getClass());

        //when
        mockMvc.perform(
                multipart("/upload/{id}", ID)
                        .file(multipartFile)
                        .header("remote-user", "unprivileged")
        )
                .andExpect(status().isForbidden());

        //then
        verifyNoInteractions(storageService);
    }

    @Disabled("Need to understand what exception being thrown")
    @Test
    @SneakyThrows
    public void errorWhenFileUploadedWithSameNameAsExistingFile() {
        //given
        MockMultipartFile multipartFile = dataCsv(getClass());
        doThrow(new FileExitsException(ID, multipartFile.getOriginalFilename()))
                .when(storageService)
                .store(ID, multipartFile);

        //when
        mockMvc.perform(
                multipart("http://example.com/upload/{id}", ID)
                        .file(multipartFile)
                        .header("remote-user", "uploader")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse(getClass(), "errorFileExistsUpload.json")));
    }

    @Disabled("Need to understand the exceptions being thrown")
    @Test
    @SneakyThrows
    public void errorSavingFile() {
        //given
        MockMultipartFile multipartFile = dataCsv(getClass());
        doThrow(new StorageServiceException(ID, "Could not upload data.csv"))
                .when(storageService)
                .store(ID, multipartFile);

        //when
        Assertions.assertThrows(StorageServiceException.class,() -> {
            mockMvc.perform(
                    multipart("http://example.com/upload/{id}", ID)
                            .file(multipartFile)
                            .header("remote-user", "uploader")
                            .accept(MediaType.APPLICATION_JSON)
            )
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(content().json(expectedResponse(getClass(), "errorUpload.json")));
        });
    }

    @Test
    @SneakyThrows
    public void uploaderCanUploadFileWithSpaces() {
        //given
        val fileWithSpaces = fileWithSpacesCsv(getClass());

        //when
        mockMvc.perform(
                multipart("http://example.com/upload/{id}", ID)
                        .file(fileWithSpaces)
                        .header("remote-user", "uploader")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNoContent());

        //then
        verify(storageService).store(ID, fileWithSpaces);
    }
}
