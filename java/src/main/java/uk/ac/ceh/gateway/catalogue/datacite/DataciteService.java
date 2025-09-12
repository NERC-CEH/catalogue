package uk.ac.ceh.gateway.catalogue.datacite;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.time.LocalDate;
import java.util.*;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.DATACITE_JSON_VALUE;
import static uk.ac.ceh.gateway.catalogue.util.Headers.withBasicAuth;

/**
 * A service which interacts with the datacite rest api to obtain a DOI for a
 * GeminiMetadata record
 */
@Service
@Slf4j
@ToString(exclude = {"password", "restTemplate"})
public class DataciteService {
    private final String api;
    private final String prefix;
    private final String publisher;
    private final String legacyPublisher;
    private final String username;
    private final String password;
    private final DocumentIdentifierService identifierService;
    private final RestTemplate restTemplate;
    private final JenaLookupService jenaLookupService;
    private final DataciteRequestService dataciteRequestService;

    public DataciteService(
            @Value("${doi.api}") String api,
            @Value("${doi.prefix}") String prefix,
            @Value("${doi.publisher}") String publisher,
            @Value("${doi.legacyPublisher}") String legacyPublisher,
            @Value("${doi.username}") String username,
            @Value("${doi.password}") String password,
            @NonNull DocumentIdentifierService identifierService,
            @Qualifier("normal") RestTemplate restTemplate,
            @NonNull JenaLookupService jenaLookupService,
            @NonNull DataciteRequestService dataciteRequestService
    ) {
        this.api = api;
        this.prefix = prefix;
        this.publisher = publisher;
        this.legacyPublisher = legacyPublisher;
        this.username = username;
        this.password = password;
        this.identifierService = identifierService;
        this.restTemplate = restTemplate;
        this.jenaLookupService = jenaLookupService;
        this.dataciteRequestService = dataciteRequestService;
        log.info("Creating");
    }

    /**
     * Contacts the DATACITE rest api and uploads a datacite requests and then
     * gets the that request minted
     */
    public ResourceIdentifier generateDoi(GeminiDocument document) throws DataciteException {
        log.info("checking for mint");
        if(isDataciteMintable(document)) {
            val request = getDatacitationRequest(document);
            log.info("Requesting mint of doi: {}", request);
            val url = UriComponentsBuilder
                    .fromUriString(api)
                    .toUriString();
            DataciteRequest dataciteRequest = new DataciteRequest(request, identifierService.generateUri(document.getId()), jenaLookupService, dataciteRequestService);
            try {
                ObjectMapper mapper = new ObjectMapper();
                String jsonPayload = mapper.writeValueAsString(dataciteRequest);
                log.info("---- Datacite JSON Payload ----\n{}\n---- END Payload ----", jsonPayload);

                val headers = withBasicAuth(username, password);
                headers.setContentType(MediaType.valueOf(DATACITE_JSON_VALUE));
                headers.setAccept(List.of(MediaType.valueOf(DATACITE_JSON_VALUE)));
                restTemplate.postForEntity(
                        url,
                        new HttpEntity<>(dataciteRequest, headers),
                        String.class
                );
            }
            catch(HttpClientErrorException ex) {
                log.error("Failed to mint doi: {} - {} - {}", request.get("doi"), ex.getResponseBodyAsString(), ex.getStatusCode());
                throw new DataciteException("Minting of the DOI failed, please review the datacite JSON (is it valid?) then try again", ex);
            }
            catch(RestClientException | JsonProcessingException ex) {
                throw new DataciteException("Failed to communicate with the datacite api when trying to mint the doi", ex);
            }
        }
        else {
            throw new DataciteException("This record does not meet the requirements for datacite minting");
        }

        return ResourceIdentifier
                .builder()
                .code(generateDoiString(document))
                .codeSpace("doi:")
                .build();

    }

    /**
     * Grab the current metadata document from the datacite api. If this gemini
     * document does not have a doi, then return null
     * @return a populated datacite request, or null if nothing there
     */
    public DataciteRequest getDoiMetadata(GeminiDocument document) {
        if (getDoi(document).isPresent()) {
            val url = UriComponentsBuilder
                    .fromUriString(api)
                    .pathSegment(prefix, document.getId())
                    .toUriString() + "?affiliation=true&publisher=true";
            log.info("Url to retrieve DOI from Datacite: {}", url);
            try {
                HttpHeaders headers = withBasicAuth(username, password);
                headers.set("Accept", DATACITE_JSON_VALUE);

                String retrievedDoiMetadataString = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
                ).getBody();

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

                return objectMapper.readValue(retrievedDoiMetadataString, DataciteRequest.class);
            }
            catch(RestClientException ex) {
                throw new DataciteException("Failed to obtain datacite metadata", ex);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    /**
     * Contact the datacite rest api and submit a metadata request. This is only
     * possible if the document is in the correct format
     * @param document to submit a metadata datacite request of
     */
    public void updateDoiMetadata(GeminiDocument document) {
        if(isDatacitable(document, true)) {
            try {
                val headers = withBasicAuth(username, password);
                headers.setContentType(MediaType.valueOf(DATACITE_JSON_VALUE));
                headers.setAccept(List.of(MediaType.valueOf(DATACITE_JSON_VALUE)));
                val request = getDatacitationRequest(document);
                val url = UriComponentsBuilder
                        .fromUriString(api)
                        .pathSegment(prefix, document.getId())
                        .toUriString();

                DataciteRequest dataciteRequest = new DataciteRequest(request, identifierService.generateUri(document.getId()), jenaLookupService, dataciteRequestService);
                restTemplate.exchange(
                        url,
                        HttpMethod.PUT,
                        new HttpEntity<>(dataciteRequest, headers),
                        String.class
                );

            }
            catch(HttpClientErrorException ex) {
                log.error("Failed to upload doi: {} - {}", generateDoiString(document), ex.getResponseBodyAsString());
                throw new DataciteException("Failed to submit the doi metadata, please review the datacite JSON (is it valid?) then try again", ex);
            }
            catch(RestClientException ex) {
                throw new DataciteException("Failed to communicate with the datacite api when trying to upload a record", ex);
            }
        }
        else {
            throw new DataciteException("This record does not meet the requirements for datacite updating");
        }
    }

    /**
     * Determines if the specified gemini document can be datacited by this
     * datacite service
     * @param document to test if ready for data citation
     * @return true if this GeminiDocument can be submitted to datacite
     */

    public boolean isDatacitable(GeminiDocument document, boolean canBeLegacy) {
        boolean isPubliclyViewable = Optional.ofNullable(document.getMetadata())
                .map(m -> m.isPubliclyViewable(Permission.VIEW))
                .orElse(false);
        boolean hasNonEmptyTitle = !Strings.isNullOrEmpty(document.getTitle());
        boolean hasAuthor = Optional.ofNullable(document.getResponsibleParties())
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch((p) -> "author".equals(p.getRole()));
        boolean hasCorrectPublisher;
        if(canBeLegacy){
            hasCorrectPublisher = Optional.ofNullable(document.getResponsibleParties())
                .orElse(Collections.emptyList())
                .stream()
                .filter((p) -> "publisher".equals(p.getRole()))
                .anyMatch((p) -> publisher.equals(p.getOrganisationName()) || legacyPublisher.equals(p.getOrganisationName()));
        }else{
            hasCorrectPublisher = Optional.ofNullable(document.getResponsibleParties())
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter((p) -> "publisher".equals(p.getRole()))
                    .anyMatch((p) -> publisher.equals(p.getOrganisationName()));
        }

        boolean hasPublicationYear = Optional.ofNullable(document.getDatasetReferenceDate())
                .map(DatasetReferenceDate::getPublicationDate)
                .map((d) -> !d.isAfter(LocalDate.now()))
                .orElse(false);
        return isPubliclyViewable
                && hasNonEmptyTitle
                && hasAuthor
                && hasCorrectPublisher
                && hasPublicationYear;
    }

    /**
     * Determine if this geminidocument is datacitable and has already had a doi
     * attached to it.
     * @return true if the GeminiDocument has already been submitted for a doi
     */
    public boolean isDatacited(GeminiDocument document) {
        return isDatacitable(document, true) && getDoi(document).isPresent();
    }

    /**
     * Determine if this geminidocument can be submitted for a fresh doi. This
     * means it means the doi requirements and doesn't already have a doi
     * @return true if the GeminiDocument can be submitted for a doi
     */
    public boolean isDataciteMintable(GeminiDocument document) {
        return isDatacitable(document, false) && getDoi(document).isEmpty();
    }

    private Optional<String> getDoi(GeminiDocument document) {
        return Optional.ofNullable(document.getResourceIdentifiers())
                .orElse(Collections.emptyList())
                .stream()
                .filter((i) -> i.getCodeSpace().equals("doi:"))
                .map(ResourceIdentifier::getCode)
                .filter(code -> code.startsWith(prefix))
                .findFirst();
    }

    /**
     * Process the datacitation request template for the given document and
     * return the request as a Map.
     * @param document to prepare a datacitation request for
     * @return a Map request
     */
    public Map<String, Object> getDatacitationRequest(GeminiDocument document) {
        String doi = generateDoiString(document);
        Map<String, Object> data = new HashMap<>();
        data.put("doc", document);
        data.put("resourceType", getDataciteResourceType(document));
        data.put("doi", doi);

        return data;
    }

    private String getDataciteResourceType(MetadataDocument document) {
        switch(document.getType()) {
            case "nonGeographicDataset":
            case "dataset":              return "Dataset";
            case "application":          return "Other";
            case "model":                return "Model";
            case "service":              return "Service";
            case "software":             return "Software";
            default:                     return null;
        }
    }

    private String generateDoiString(GeminiDocument document) {
        return prefix + "/" + document.getId();
    }

    public DataciteRequest getNewDataciteRequest(GeminiDocument document) {
        return new DataciteRequest(getDatacitationRequest(document), identifierService.generateUri(document.getId()), jenaLookupService, dataciteRequestService);
    }

}
