package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ceh.gateway.catalogue.config.WebConfig;

import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadControllerUtils.ID;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadControllerUtils.expectedResponse;

/**
 * Test Upload Controller delete file endpoint
 */

@ActiveProfiles({"development", "upload:simple"})
@TestPropertySource("UploadControllerTest.properties")
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfig.class, UploadControllerUtils.TestConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
// DirtiesContext needed as StorageService is a Mock that needs refreshing before each test
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UploadControllerDeleteTest {
    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;
    @Autowired
    private StorageService storageService;
    private final String filename = "test.csv";

    @Before
    public void setup() {
        mockMvc = webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    @Test
    @SneakyThrows
    public void uploaderCanDeleteFile() {
        //given
        given(storageService.filenames(ID)).willReturn(Stream.of("data1.csv", "data2.csv"));

        //when
        mockMvc.perform(
                delete("http://example.com/upload/{id}/{filename}", ID, filename)
                        .header("remote-user", "uploader")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse(getClass(),"successfulDeletion.json")));

        //then
        verify(storageService).delete(ID, filename);
    }

    @Test
    @SneakyThrows
    public void unprivilegedUserCanNotDeleteFile() {
        mockMvc.perform(
                delete("http://example.com/upload/{id}/{filename}", ID, filename)
                        .header("remote-user", "unprivileged")
        )
                .andExpect(status().isForbidden());

        //then
        verifyZeroInteractions(storageService);
    }

    @Test
    @SneakyThrows
    public void errorDeletingFile() {
        //given
        doThrow(new RuntimeException()).when(storageService).delete(ID, filename);

        //when
        mockMvc.perform(
                delete("http://example.com/upload/{id}/{filename}", ID, filename)
                        .header("remote-user", "uploader")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse(getClass(),"errorDeletion.json")));
    }

    @Test
    @SneakyThrows
    public void canDeleteFileWithSpacesInName() {
        //given
        val filenameWithSpaces = "data with spaces.csv";
        given(storageService.filenames(ID)).willReturn(Stream.of("data1.csv", "data2.csv"));

        //when
        mockMvc.perform(
                delete("http://example.com/upload/{id}/{filename}", ID, filenameWithSpaces)
                        .header("remote-user", "uploader")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse(getClass(), "successfulDeletionWithSpaces.json")));

        //then
        verify(storageService).delete(ID, filenameWithSpaces);
    }
}