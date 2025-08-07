package uk.ac.ceh.gateway.catalogue.services;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.util.Headers;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
public class LegiloSupportingDocsService {

    private final RestClient restClient;

    public LegiloSupportingDocsService(
        @Qualifier("normal") RestTemplate template,
        @Value("${legilo.url}") String legiloUrl,
        @Value("${legilo.user}") String legiloUser,
        @Value("${legilo.password}") String legiloPassword
    ) {
        val authHeaders = Headers.withBasicAuth(legiloUser, legiloPassword);
        this.restClient = RestClient.builder(template)
            .baseUrl(legiloUrl)
            .defaultHeaders(headers -> headers.addAll(authHeaders))
            .build();
        log.info("Created LegiloSupportingDocsService for {}", legiloUrl);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getFulltextByDatasetId(String datasetId) {
        try {
            Map<String, String> result = restClient.get()
                .uri("/{datasetId}/supporting-docs", datasetId)
                .retrieve()
                .body(Map.class);

            if (result == null || result.isEmpty()) {
                log.info("No supporting docs found for {}", datasetId);
                return Collections.emptyMap();
            }

            log.info("Retrieved {} docs from Legilo for {}", result.size(), datasetId);
            return result;

        } catch (Exception e) {
            log.warn("Failed to call Legilo supporting docs for {}: {}", datasetId, e.toString());
            return Collections.emptyMap();
        }
    }
}
