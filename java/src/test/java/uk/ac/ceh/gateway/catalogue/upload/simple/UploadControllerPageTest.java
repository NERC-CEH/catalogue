package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
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

import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadControllerUtils.ID;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadControllerUtils.TITLE;

/**
 * Testing the Upload Controller HTML page endpoint
 */
@ActiveProfiles({"development", "upload:simple"})
@TestPropertySource("UploadControllerTest.properties")
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfig.class, UploadControllerUtils.TestConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
// DirtiesContext needed as StorageService is a Mock that needs refreshing before each test
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UploadControllerPageTest {

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
                get("/upload/{id}", ID)
                    .header("remote-user", "uploader")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("id", ID))
                .andExpect(model().attribute("title", TITLE))
                .andExpect(model().attributeExists("files"))
                .andExpect(flash().attributeCount(0))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        //then
        verify(storageService).filenames(ID);
    }

    @Test
    @SneakyThrows
    public void adminCanAccessPage() {
        mockMvc.perform(
                get("/upload/{id}", ID)
                    .header("remote-user", "admin")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("id", ID))
                .andExpect(model().attribute("title", TITLE))
                .andExpect(model().attributeExists("files"))
                .andExpect(flash().attributeCount(0))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        //then
        verify(storageService).filenames(ID);
    }

    @Test
    @SneakyThrows
    public void unprivilegedUserCanNotAccessPage() {
        mockMvc.perform(
                get("/upload/{id}", ID)
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
                get("/upload/{id}", ID)
        )
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        //then
        verifyZeroInteractions(storageService);
    }

    @Test
    @SneakyThrows
    public void errorDisplayingFilesOnPage() {
        //given
        doThrow(new RuntimeException()).when(storageService).filenames(ID);

        //when
        mockMvc.perform(
                get("/upload/{id}", ID)
                        .header("remote-user", "uploader")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("id", ID))
                .andExpect(model().attribute("title", TITLE))
                .andExpect(model().attribute("files", emptyCollectionOf(UploadController.FileInfo.class)))
                .andExpect(model().attribute("error", "Failed to retrieve information"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        //then
        verify(storageService).filenames(ID);
    }
}