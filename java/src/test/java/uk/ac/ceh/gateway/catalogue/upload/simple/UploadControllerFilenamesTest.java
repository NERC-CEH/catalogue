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

import java.util.Arrays;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadControllerUtils.ID;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadControllerUtils.expectedResponse;

/**
 * Test Upload Controller filenames JSON endpoint
 */
@ActiveProfiles({"development", "upload:simple"})
@TestPropertySource("UploadControllerTest.properties")
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfig.class, UploadControllerUtils.TestConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
// DirtiesContext needed as StorageService is a Mock that needs refreshing before each test
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UploadControllerFilenamesTest {

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
    public void uploaderCanGetListOfFilenames() {
        //given
        val filenames = Arrays.asList(
                new FileInfo("data1.csv"),
                new FileInfo("data2.csv"),
                new FileInfo("name with spaces.csv")
        );
        given(storageService.filenames(ID)).willReturn(filenames);

        mockMvc.perform(
                get("http://example.com/upload/{id}", ID)
                .header("remote-user", "uploader")
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse(getClass(),"successfulFilenames.json")));

    }

    @Test
    @SneakyThrows
    public void unprivilegedUserCanNotGetListOfFilenames() {

        mockMvc.perform(
                get("http://example.com/upload/{id}", ID)
                        .header("remote-user", "unprivileged")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        verifyZeroInteractions(storageService);
    }

    @Test
    @SneakyThrows
    public void unauthenticatedUserCanNotGetListOfFilenames() {

        mockMvc.perform(
                get("http://example.com/upload/{id}", ID)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        verifyZeroInteractions(storageService);
    }

    @Test
    @SneakyThrows
    public void unknownFilenames() {
        //given
        doThrow(new UserInputException(ID, "Could not retrieve files")).when(storageService).filenames(ID);

        mockMvc.perform(
                get("http://example.com/upload/{id}", ID)
                        .header("remote-user", "uploader")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse(getClass(), "unknownFilenames.json")));
    }

    @Test
    @SneakyThrows
    public void errorMessageGettingFilenames() {
        //given
        doThrow(new StorageServiceException(ID, "Could not retrieve files")).when(storageService).filenames(ID);

        mockMvc.perform(
                get("http://example.com/upload/{id}", ID)
                        .header("remote-user", "uploader")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse(getClass(), "errorFilenames.json")));
    }
}