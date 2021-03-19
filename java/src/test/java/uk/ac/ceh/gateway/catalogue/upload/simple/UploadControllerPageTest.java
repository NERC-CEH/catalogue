package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ceh.gateway.catalogue.config.WebConfig;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadControllerUtils.*;

/**
 * Testing the Upload Controller HTML page endpoint
 */
@Disabled
@ActiveProfiles({"development", "upload:simple"})
@TestPropertySource("UploadControllerTest.properties")
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfig.class, UploadControllerUtils.TestConfig.class})
@ExtendWith(SpringExtension.class)
// DirtiesContext needed as StorageService is a Mock that needs refreshing before each test
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UploadControllerPageTest {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;
    @Autowired
    private StorageService storageService;
    private final FileInfo data1 = new FileInfo("data1.csv");
    private final FileInfo data2 = new FileInfo("data2.csv");
    private final FileInfo oneWithSpaces = new FileInfo("one with spaces.txt");
    private final List<FileInfo> fileInfos = Arrays.asList(
            data1,
            data2,
            oneWithSpaces
    );

    @BeforeEach
    public void setup() {
        mockMvc = webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    @Test
    @SneakyThrows
    public void uploaderCanAccessPage() {
        //given
        given(storageService.filenames(ID)).willReturn(fileInfos);

        //when
        mockMvc.perform(
                get("/upload/{id}", ID)
                    .header("remote-user", "uploader")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("id", ID))
                .andExpect(model().attribute("title", TITLE))
                .andExpect(model().attribute("catalogueKey", CATALOGUE))
                .andExpect(model().attribute("files", hasItems(data1, data2, oneWithSpaces)))
                .andExpect(model().attributeDoesNotExist("message"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        //then
        verify(storageService).filenames(ID);
    }

    @Test
    @SneakyThrows
    public void adminCanAccessPage() {
        //given
        given(storageService.filenames(ID)).willReturn(fileInfos);

        //when
        mockMvc.perform(
                get("/upload/{id}", ID)
                    .header("remote-user", "admin")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("id", ID))
                .andExpect(model().attribute("title", TITLE))
                .andExpect(model().attribute("catalogueKey", CATALOGUE))
                .andExpect(model().attribute("files", hasItems(data1, data2, oneWithSpaces)))
                .andExpect(model().attributeDoesNotExist("message"))
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
        verifyNoInteractions(storageService);
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
        verifyNoInteractions(storageService);
    }

    @Test
    @SneakyThrows
    public void errorDisplayingFilesOnPage() {
        //given
        val expectedMessage = new UploadController.ErrorMessage(
                "Failed to retrieve file information for 993c5778-e139-4171-a57f-7a0f396be4b8"
        );
        doThrow(new StorageServiceException(ID, "Failed to retrieve file information")).when(storageService).filenames(ID);

        //when
        mockMvc.perform(
                get("/upload/{id}", ID)
                        .header("remote-user", "uploader")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("id", ID))
                .andExpect(model().attribute("title", TITLE))
                .andExpect(model().attribute("catalogueKey", CATALOGUE))
//                .andExpect(model().attribute("files", emptyCollectionOf(String.class)))
                .andExpect(model().attribute("message", equalTo(expectedMessage)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        //then
        verify(storageService).filenames(ID);
    }
}