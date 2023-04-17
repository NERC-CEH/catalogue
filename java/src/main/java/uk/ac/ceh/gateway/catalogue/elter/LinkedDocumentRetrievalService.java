package uk.ac.ceh.gateway.catalogue.elter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

@Profile("elter")
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
            JsonNode jsonRecordAttributes = objectMapper.readTree(new URL(dataciteRecordUrl)).get("data").get("attributes");

            // create document from Datacite response
            ElterDocument document = new ElterDocument();
            document.setTitle(jsonRecordAttributes.get("titles").get(0).get("title").asText());
            // fixed stuff
            document.setDataLevel("Level 0");
            document.setType("signpost");

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
                        ex.getRawStatusCode(),
                        ex.getStatusText(),
                        ex.getResponseHeaders(),
                        ex.getResponseBodyAsString()
                        );
                throw ex;
            }
        }
    }
}
