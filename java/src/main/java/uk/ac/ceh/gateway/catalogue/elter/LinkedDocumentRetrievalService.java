package uk.ac.ceh.gateway.catalogue.elter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
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

import uk.ac.ceh.gateway.catalogue.gemini.AccessLimitation;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

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
            // ensure title is set to something
            JsonNode jsonTitles = jsonRecordAttributes.get("titles").path(0);
            if (jsonTitles.isMissingNode()){
                document.setTitle("TITLE MISSING");
            }
            else {
                document.setTitle(jsonTitles.get("title").asText());
            }
            // description
            JsonNode jsonDescriptions = jsonRecordAttributes.get("descriptions").path(0);
            if (! jsonDescriptions.isMissingNode()){
                document.setDescription(jsonDescriptions.get("description").asText());
            }
            // authors
            JsonNode jsonCreators = jsonRecordAttributes.get("creators").path(0);
            if (! jsonCreators.isMissingNode()){
                ResponsibleParty documentCreators = ResponsibleParty.builder()
                        .individualName(jsonCreators.get("name").asText())
                        .organisationName("Unknown")
                        .role("author")
                        .build();
                ArrayList<ResponsibleParty> list1 = new ArrayList<>();
                list1.add(documentCreators);
                document.setResponsibleParties(list1);
            }
            // onlineresources
            ArrayList<OnlineResource> list2 = new ArrayList<>();
            list2.add(
                    OnlineResource.builder()
                    .url(jsonRecordAttributes.get("url").asText())
                    .name("View record")
                    .description("View record at this link")
                    .function("information")
                    .build()
                    );
            document.setOnlineResources(list2);
            // reference dates
            // the timestamp parsing is extremely dubious but we need working code NOW for the Frankfurt meeting.
            // TBF using LocalDate is totally broken anyway so it all needs redoing at some point.
            document.setDatasetReferenceDate(
                    DatasetReferenceDate.builder()
                    .creationDate(LocalDate.parse(jsonRecordAttributes.get("created").asText().substring(0,10)))
                    .publicationDate(LocalDate.parse(jsonRecordAttributes.get("published").asText().substring(0,4) + "-01-01"))
                    .creationDate(LocalDate.parse(jsonRecordAttributes.get("created").asText().substring(0,10)))
                    .build()
                    );
            // import ID - should equal input doi but can't be too careful
            document.setImportId(jsonRecordAttributes.get("doi").asText());
            // fixed stuff
            document.setAccessLimitation(
                    AccessLimitation.builder()
                    .value("no limitations to public access")
                    .code("Available")
                    .uri("http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations")
                    .build()
                    );
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
