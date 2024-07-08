package uk.ac.ceh.gateway.catalogue.elter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Profile("server:elter")
@Slf4j
@Service
@ToString
public class LinkedDocumentRetrievalService {
    private final ObjectMapper objectMapper;
    private final Pattern p;
    private final RestTemplate restTemplate;
    private final String dataciteApiRoot;

    public LinkedDocumentRetrievalService(
            @Qualifier("normal") RestTemplate restTemplate,
            @Value("${doi.api}") String dataciteApiRoot
            ) {
        this.objectMapper = new ObjectMapper();
        this.p = Pattern.compile("10\\.\\S+/\\S+");
        this.restTemplate = restTemplate;
        this.dataciteApiRoot = dataciteApiRoot;
        log.info("Creating");
            }

    @SneakyThrows
    public ElterDocument get(String url) {
        // check if the input contains a DOI
        Matcher doiCheck = p.matcher(url);
        if (doiCheck.find()) {
            // get DOI
            String inputDoi = doiCheck.group(0);
            log.info("DOI DETECTED: {}", inputDoi);

            // call Datacite
            String dataciteRecordUrl = dataciteApiRoot + "/" + inputDoi;
            log.info("GET {}", dataciteRecordUrl);
            JsonNode dataciteJson = objectMapper.readTree(new URI(dataciteRecordUrl).toURL());

            // create and return ElterDocument
            ElterDocument document = new ElterDocument();
            document.importDataciteJson(dataciteJson);
            document.setImportId(inputDoi);
            return document;
        }
        else {
            log.info("GET {}", url);
            try {
                val headers = new HttpHeaders();
                headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                val response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        ElterDocument.class
                        );
                log.info("Response {}", response);
                return response.getBody();
            } catch (RestClientResponseException ex) {
                log.error(
                        "Error communicating with supplied URL: (statusCode={}, status={}, headers={}, body={})",
                        ex.getStatusCode().value(),
                        ex.getStatusText(),
                        ex.getResponseHeaders(),
                        ex.getResponseBodyAsString()
                        );
                throw ex;
            }
        }
    }
}
