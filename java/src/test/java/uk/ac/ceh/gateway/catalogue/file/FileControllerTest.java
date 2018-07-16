package uk.ac.ceh.gateway.catalogue.file;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class FileControllerTest {
    private MockMvc mockMvc;

    @Mock
    private FileRepository fileRepository;

    String json = String.valueOf(getClass().getResourceAsStream("dataset0.json"));

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new FileController(fileRepository)).build();
    }

    @Test
    @SneakyThrows
    public void successfullyCreate() {
        //given
        given(fileRepository.create(json)).willReturn("test0");

        //when
        mockMvc.perform(post("/file")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/file/test0"));

        //then
        verify(fileRepository).create(json);
    }

    @Test
    @SneakyThrows
    public void wrongContentTypeCreate() {
        //when
        mockMvc.perform(post("/file")
            .content(json)
            .contentType(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(status().isUnsupportedMediaType())
            .andExpect(header().doesNotExist("Location"));

        //then
        verify(fileRepository, never()).create(anyString());
    }

    @Test
    @SneakyThrows
    public void noContentCreate() {
        //when
        mockMvc.perform(post("/file")
            .contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isBadRequest())
            .andExpect(header().doesNotExist("Location"));

        //then
        verify(fileRepository, never()).create(anyString());
    }

    @Test
    @SneakyThrows
    public void successfullyRead() {
        //given
        given(fileRepository.read("test0")).willReturn(json);

        //when
        mockMvc.perform(get("/file/test0")
            .accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk())
            .andExpect(content().string(json));

        //then
        verify(fileRepository).read("test0");
    }

    @Test
    @SneakyThrows
    public void wrongAcceptRead() {
        //when
        mockMvc.perform(get("/file/test0")
            .accept(MediaType.TEXT_PLAIN))
            .andExpect(status().isNotAcceptable())
            .andExpect(content().string(""));

        //then
        verify(fileRepository, never()).read("test0");
    }

    @Test
    @SneakyThrows
    public void unknownIdRead() {
        //given
        given(fileRepository.read("unknown")).willThrow(new UnknownFileException());

        //when
        mockMvc.perform(get("/file/unknown")
            .accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));

        //then
        verify(fileRepository).read("unknown");
    }

    @Test
    @SneakyThrows
    public void ioErrorRead() {
        //given
        given(fileRepository.read("test0")).willThrow(new IOException());

        //when
        mockMvc.perform(get("/file/test0")
            .accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(""));

        //then
        verify(fileRepository).read("test0");
    }

    @Test
    @SneakyThrows
    public void successfullyUpdate() {
        //when
        mockMvc.perform(put("/file/test0")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(json))
            .andExpect(status().isNoContent());

        //then
        verify(fileRepository).update("test0", json);
    }

    @Test
    @SneakyThrows
    public void noContentUpdate() {
        //when
        mockMvc.perform(put("/file/test0")
            .contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isBadRequest());

        //then
        verify(fileRepository, never()).update(eq("test0"), anyString());
    }

    @Test
    @SneakyThrows
    public void successfullyDelete() {
        //when
        mockMvc.perform(delete("/file/test0"))
            .andExpect(status().isNoContent());

        //then
        verify(fileRepository).delete("test0");
    }

    @Test
    @SneakyThrows
    public void unknownDelete() {
        //given
        willThrow(new UnknownFileException()).given(fileRepository).delete("unknown");
        //when
        mockMvc.perform(delete("/file/unknown"))
            .andExpect(status().isNotFound());

        //then
        verify(fileRepository).delete("unknown");
    }
}