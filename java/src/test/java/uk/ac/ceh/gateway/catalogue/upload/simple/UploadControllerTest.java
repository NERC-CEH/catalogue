package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ceh.gateway.catalogue.config.WebConfig;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ActiveProfiles({"development", "upload:simple"})
@TestPropertySource
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfig.class, UploadControllerTest.TestConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
// DirtiesContext needed as StorageService is a Mock that needs refreshing before each test
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UploadControllerTest {
    @Autowired
    WebApplicationContext wac;
    MockMvc mockMvc;
    @Autowired
    StorageService storageService;

    final String id = "993c5778-e139-4171-a57f-7a0f396be4b8";
    final String title = "Belowground carbon stock data in the Ankeniheny Zahamena forest corridor, Madagascar";

    // Needed as @MockBean only available in Spring Boot
    public static class TestConfig {
        @Bean
        public StorageService storageService() {
            return mock(StorageService.class);
        }
    }

    @SneakyThrows
    MockMultipartFile dataCsv() {
        return new MockMultipartFile(
                "file",
                "data.csv",
                "text/csv",
                IOUtils.toByteArray(getClass().getResourceAsStream("data.csv"))
        );
    }

    @SneakyThrows
    MockMultipartFile fileWithSpacesCsv() {
        return new MockMultipartFile(
                "file",
                "file with spaces.csv",
                "text/csv",
                IOUtils.toByteArray(getClass().getResourceAsStream("file with spaces.csv"))
        );
    }

    @SneakyThrows
    String expectedResponse(String filename) {
        return StreamUtils.copyToString(
                getClass().getResourceAsStream(filename),
                StandardCharsets.UTF_8
        );
    }

    @Before
    public void setup() {
        mockMvc = webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }
}