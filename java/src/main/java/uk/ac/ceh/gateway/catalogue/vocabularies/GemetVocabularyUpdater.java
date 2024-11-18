package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.TimeConstants;

import java.io.FileOutputStream;
import java.io.IOException;


/**
 GemetVocabularyUpdater is responsible for fetching the GEMET vocabulary data from a remote URL and storing it locally. This class ensures the GEMET vocabulary is periodically updated and stored in a local file for use within the application.
 */
@Slf4j
@Component
public class GemetVocabularyUpdater implements VocabularyUpdater {

    private final RestTemplate restTemplate;
    private final String gemetRemoteUrl;
    private final String gemetLocalPath;

    public GemetVocabularyUpdater(
        @Qualifier("normal") RestTemplate restTemplate,
        @Value("${gemet.concepturl}") String gemetRemoteUrl,
        @Value("${gemet.local}") String gemetLocalPath
    ) {
        this.restTemplate = restTemplate;
        this.gemetRemoteUrl = gemetRemoteUrl;
        this.gemetLocalPath = gemetLocalPath;
    }

    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE, fixedDelay = TimeConstants.SEVEN_DAYS)
    @Override
    public void updateVocabulary() {
        log.info("Updating GEMET vocabulary");

        try {
            // Fetch vocabulary data from remote URL
            byte[] data = restTemplate.getForObject(gemetRemoteUrl, byte[].class);

            // Write fetched data to the local path
            try (FileOutputStream fos = new FileOutputStream(gemetLocalPath)) {
                fos.write(data);
            }

            log.info("Successfully updated GEMET vocabulary");
        } catch (IOException e) {
            log.error("Failed to store GEMET vocabulary");
            throw new RuntimeException("Failed to store GEMET vocabulary", e);
        } catch (Exception e) {
            log.error("Failed to retrieve GEMET vocabulary from {}", gemetRemoteUrl, e);
            throw new RuntimeException("Failed to retrieve GEMET vocabulary", e);
        }
    }
}
