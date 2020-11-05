package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test Upload Controller delete file endpoint
 */
public class UploadControllerDeleteTest extends AbstractUploadControllerTest {

    private final String filename = "test.csv";

    @Test
    @SneakyThrows
    public void uploaderCanDeleteFile() {
        //given
        given(storageService.filenames(id)).willReturn(Stream.of("data1.csv", "data2.csv"));

        //when
        mockMvc.perform(
                delete("http://example.com/upload/{id}/{filename}", id, filename)
                        .header("remote-user", "uploader")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse("successfulDeletion.json")));

        //then
        verify(storageService).delete(id, filename);
    }

    @Test
    @SneakyThrows
    public void unprivilegedUserCanNotDeleteFile() {
        mockMvc.perform(
                delete("http://example.com/upload/{id}/{filename}", id, filename)
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
        doThrow(new RuntimeException()).when(storageService).delete(id, filename);

        //when
        mockMvc.perform(
                delete("http://example.com/upload/{id}/{filename}", id, filename)
                        .header("remote-user", "uploader")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse("errorDeletion.json")));
    }

    @Test
    @SneakyThrows
    public void canDeleteFileWithSpacesInName() {
        //given
        val filenameWithSpaces = "data with spaces.csv";
        given(storageService.filenames(id)).willReturn(Stream.of("data1.csv", "data2.csv"));

        //when
        mockMvc.perform(
                delete("http://example.com/upload/{id}/{filename}", id, filenameWithSpaces)
                        .header("remote-user", "uploader")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse("successfulDeletionWithSpaces.json")));

        //then
        verify(storageService).delete(id, filenameWithSpaces);
    }
}