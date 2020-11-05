package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.FileAlreadyExistsException;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test Upload Controller file upload endpoint
 */
public class UploadControllerUploadTest extends AbstractUploadControllerTest {

    private final MockMultipartFile multipartFile = dataCsv();

    @Test
    @SneakyThrows
    public void uploaderCanUploadFile() {
        //given
        given(storageService.filenames(id)).willReturn(Stream.of("data.csv", "data1.csv", "data2.csv"));

        //when
        mockMvc.perform(
                fileUpload("http://example.com/upload/{id}", id)
                        .file(multipartFile)
                        .header("remote-user", "uploader")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse("successfulUpload.json")));

        //then
        verify(storageService).store(id, multipartFile);
    }

    @Test
    @SneakyThrows
    public void unprivilegedUserCanNotUploadFile() {
        mockMvc.perform(
                fileUpload("/upload/{id}", id)
                        .file(multipartFile)
                        .header("remote-user", "unprivileged")
        )
                .andExpect(status().isForbidden());

        //then
        verifyZeroInteractions(storageService);
    }

    @Test
    @SneakyThrows
    public void errorWhenFileUploadedWithSameNameAsExistingFile() {
        //given
        doThrow(new FileAlreadyExistsException("data.csv")).when(storageService).store(id, multipartFile);

        //when
        mockMvc.perform(
                fileUpload("http://example.com/upload/{id}", id)
                        .file(multipartFile)
                        .header("remote-user", "uploader")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse("errorFileExistsUpload.json")));
    }

    @Test
    @SneakyThrows
    public void errorSavingFile() {
        //given
        doThrow(new RuntimeException()).when(storageService).store(id, multipartFile);

        //when
        mockMvc.perform(
                fileUpload("http://example.com/upload/{id}", id)
                        .file(multipartFile)
                        .header("remote-user", "uploader")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse("errorUpload.json")));
    }

    @Test
    @SneakyThrows
    public void uploaderCanUploadFileWithSpaces() {
        //given
        val fileWithSpaces = fileWithSpacesCsv();
        given(storageService.filenames(id)).willReturn(Stream.of("data.csv", "data1.csv", "data2.csv"));

        //when
        mockMvc.perform(
                fileUpload("http://example.com/upload/{id}", id)
                        .file(fileWithSpaces)
                        .header("remote-user", "uploader")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse("successfulUploadWithSpaces.json")));

        //then
        verify(storageService).store(id, fileWithSpaces);
    }
}