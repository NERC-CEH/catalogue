package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test Upload Controller filenames JSON endpoint
 */
public class UploadControllerFilenamesTest extends AbstractUploadControllerTest {

    @Test
    @SneakyThrows
    public void uploaderCanGetListOfFilenames() {
        //given
        given(storageService.filenames(id)).willReturn(Stream.of("data1.csv", "data2.csv"));

        mockMvc.perform(
                get("http://example.com/upload/{id}", id)
                .header("remote-user", "uploader")
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse("successfulFilenames.json")));

    }

    @Test
    @SneakyThrows
    public void unprivilegedUserCanNotGetListOfFilenames() {

        mockMvc.perform(
                get("http://example.com/upload/{id}", id)
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
                get("http://example.com/upload/{id}", id)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        verifyZeroInteractions(storageService);
    }

    @Test
    @SneakyThrows
    public void errorMessageGettingFilenames() {
        //given
        doThrow(new RuntimeException()).when(storageService).filenames(id);

        mockMvc.perform(
                get("http://example.com/upload/{id}", id)
                        .header("remote-user", "uploader")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse("errorFilenames.json")));
    }
}