package uk.ac.ceh.gateway.catalogue.templateHelpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileListServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private FileListService service;

    private static final String BASE_URI = "http://localhost:8080";
    private static final String DATASET_ID = "123456789";

    @BeforeEach
    void setUp() {
        service = new FileListService(BASE_URI, restTemplate);
    }

    @Test
    void getFileListOnlyFiles() {
        // Given
        List<FileListService.FileListInfo> mockResponse = List.of(
            new FileListService.FileListInfo("file1.csv", "file"),
            new FileListService.FileListInfo("file2.csv", "file")
        );

        when(restTemplate.exchange(
            eq(BASE_URI + "/datastore/eidchub/" + DATASET_ID + "/?format=json"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        List<String> result = service.getFileList(DATASET_ID);

        // Then
        assertThat(result).containsExactly("file1.csv", "file2.csv");
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class));
    }

    @Test
    void getFileListDirectoryWithFiles() {
        List<FileListService.FileListInfo> rootResponse = List.of(
            new FileListService.FileListInfo("data", "directory")
        );

        List<FileListService.FileListInfo> dataResponse = List.of(
            new FileListService.FileListInfo("data1.csv", "file"),
            new FileListService.FileListInfo("data2.csv", "file")
        );

        when(restTemplate.exchange(
            eq(BASE_URI + "/datastore/eidchub/" + DATASET_ID + "/?format=json"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(rootResponse, HttpStatus.OK));

        when(restTemplate.exchange(
            eq(BASE_URI + "/datastore/eidchub/" + DATASET_ID + "/data/?format=json"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(dataResponse, HttpStatus.OK));

        // When
        List<String> result = service.getFileList(DATASET_ID);

        // Then
        assertThat(result).containsExactly("data1.csv", "data2.csv");
        verify(restTemplate, times(2)).exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class));
    }

    @Test
    void getFileListNestedDirectories() {
        // Given
        List<FileListService.FileListInfo> rootResponse = List.of(
            new FileListService.FileListInfo("pet", "directory")
        );

        List<FileListService.FileListInfo> petResponse = List.of(
            new FileListService.FileListInfo("cat", "directory"),
            new FileListService.FileListInfo("dog.csv", "file")
        );

        List<FileListService.FileListInfo> catResponse = List.of(
            new FileListService.FileListInfo("meow.csv", "file"),
            new FileListService.FileListInfo("purr.csv", "file")
        );

        when(restTemplate.exchange(
            eq(BASE_URI + "/datastore/eidchub/" + DATASET_ID + "/?format=json"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(rootResponse, HttpStatus.OK));

        when(restTemplate.exchange(
            eq(BASE_URI + "/datastore/eidchub/" + DATASET_ID + "/pet/?format=json"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(petResponse, HttpStatus.OK));

        when(restTemplate.exchange(
            eq(BASE_URI + "/datastore/eidchub/" + DATASET_ID + "/pet/cat/?format=json"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(catResponse, HttpStatus.OK));

        // When
        List<String> result = service.getFileList(DATASET_ID);

        // Then
        assertThat(result).containsExactly( "meow.csv", "purr.csv", "dog.csv");
        verify(restTemplate, times(3)).exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class));
    }

    @Test
    void getFileListEmpty() {
        // Given
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(List.of(), HttpStatus.OK));

        // When
        List<String> result = service.getFileList(DATASET_ID);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getFileListNull() {
        // Given
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        // When
        List<String> result = service.getFileList(DATASET_ID);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getFileListMultipleDirectoriesAtRoot() {
        // Given
        List<FileListService.FileListInfo> rootResponse = List.of(
            new FileListService.FileListInfo("dir1", "directory"),
            new FileListService.FileListInfo("dir2", "directory")
        );

        List<FileListService.FileListInfo> dir1Response = List.of(
            new FileListService.FileListInfo("file1.csv", "file")
        );

        List<FileListService.FileListInfo> dir2Response = List.of(
            new FileListService.FileListInfo("file2.csv", "file")
        );

        when(restTemplate.exchange(
            eq(BASE_URI + "/datastore/eidchub/" + DATASET_ID + "/?format=json"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(rootResponse, HttpStatus.OK));

        when(restTemplate.exchange(
            eq(BASE_URI + "/datastore/eidchub/" + DATASET_ID + "/dir1/?format=json"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(dir1Response, HttpStatus.OK));

        when(restTemplate.exchange(
            eq(BASE_URI + "/datastore/eidchub/" + DATASET_ID + "/dir2/?format=json"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(dir2Response, HttpStatus.OK));

        // When
        List<String> result = service.getFileList(DATASET_ID);

        // Then
        assertThat(result).containsExactly("file1.csv", "file2.csv");
        verify(restTemplate, times(3)).exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class));
    }

}
