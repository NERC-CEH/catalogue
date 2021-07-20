package uk.ac.ceh.gateway.catalogue.elter;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Profile("elter")
@Slf4j
@Service
@ToString
public class LinkedDocumentRetrievalService {
    private final RestTemplate restTemplate;

    public LinkedDocumentRetrievalService(
            @Qualifier("normal") RestTemplate restTemplate
    ) {
        this.restTemplate = restTemplate;
        log.info("Creating");
    }

    public ElterDocument get(String url) {
        log.debug("GET {}", url);
        try {
            val response = restTemplate.getForEntity(
                    url,
                    ElterDocument.class
            );
            log.debug("Response {}", response);
            return response.getBody();
        } catch (RestClientResponseException ex) {
            log.error(
                    "Error communicating with supplied URL: (statusCode={}, status={}, headers={}, body={})",
                    ex.getRawStatusCode(),
                    ex.getStatusText(),
                    ex.getResponseHeaders(),
                    ex.getResponseBodyAsString()
            );
            throw ex;
        }
    }
}
