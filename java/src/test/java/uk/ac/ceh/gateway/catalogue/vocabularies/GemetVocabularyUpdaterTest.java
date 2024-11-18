package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GemetVocabularyUpdaterTest {

    @Mock
    private RestTemplate restTemplate;


    String TEST_LOCAL_PATH = "test_local_path";

    @AfterEach
    public void cleanUp() throws IOException {
        // Clean up the file after the test
        Path path = Paths.get(TEST_LOCAL_PATH);
        Files.deleteIfExists(path); // This will remove the file after the test
    }

    @Test
    @SneakyThrows
    public void testUpdateVocabulary() {
        // Setup
        String remoteUrl = "http://test-gemet-url";
        byte[] dummyData = "dummy json data".getBytes();

        GemetVocabularyUpdater gemetVocabularyUpdater = new GemetVocabularyUpdater(
            restTemplate,
            remoteUrl,
            TEST_LOCAL_PATH
        );

        // Given
        given(restTemplate.getForObject(remoteUrl, byte[].class)).willReturn(dummyData);

        // When
        gemetVocabularyUpdater.updateVocabulary();

        // Then
        verify(restTemplate).getForObject(remoteUrl, byte[].class);

    }

    @Test
    @SneakyThrows
    public void failToUpdateVocabulary() {
        // Setup
        String remoteUrl = "http://invalid-url";

        GemetVocabularyUpdater gemetVocabularyUpdater = new GemetVocabularyUpdater(
            restTemplate,
            remoteUrl,
            TEST_LOCAL_PATH
        );

        // Given
        given(restTemplate.getForObject(remoteUrl, byte[].class)).willThrow(new RuntimeException("Test exception"));

        // When
        assertThrows(
            RuntimeException.class,
            gemetVocabularyUpdater::updateVocabulary
        );

        // Then
        verify(restTemplate).getForObject(remoteUrl, byte[].class);
    }
}
