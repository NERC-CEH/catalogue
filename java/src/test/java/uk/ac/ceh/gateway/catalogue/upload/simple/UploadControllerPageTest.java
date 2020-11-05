package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import org.junit.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testing the Upload Controller HTML page endpoint
 */
public class UploadControllerPageTest extends AbstractUploadControllerTest {

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
                .andExpect(model().attribute("files", emptyCollectionOf(UploadController.FileInfo.class)))
                .andExpect(model().attribute("error", "Failed to retrieve information"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        //then
        verify(storageService).filenames(id);
    }
}