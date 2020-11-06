package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;

public class UploadControllerUtils {
    public static final String ID = "993c5778-e139-4171-a57f-7a0f396be4b8";
    public static final String TITLE = "Belowground carbon stock data in the Ankeniheny Zahamena forest corridor, Madagascar";

    // Needed as @MockBean only available in Spring Boot
    public static class TestConfig {
        @Bean
        public StorageService storageService() {
            return mock(StorageService.class);
        }
    }

    @SneakyThrows
    public static MockMultipartFile dataCsv(Class clazz) {
        return new MockMultipartFile(
                "file",
                "data.csv",
                "text/csv",
                IOUtils.toByteArray(clazz.getResourceAsStream("data.csv"))
        );
    }

    @SneakyThrows
    public static MockMultipartFile fileWithSpacesCsv(Class clazz) {
        return new MockMultipartFile(
                "file",
                "file with spaces.csv",
                "text/csv",
                IOUtils.toByteArray(clazz.getResourceAsStream("file with spaces.csv"))
        );
    }

    @SneakyThrows
    public static String expectedResponse(Class clazz, String filename) {
        return StreamUtils.copyToString(
                clazz.getResourceAsStream(filename),
                StandardCharsets.UTF_8
        );
    }
}