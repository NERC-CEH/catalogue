package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.base.Strings;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.DataciteException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Permission;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A service which interacts with the datacite rest api to obtain a DOI for a 
 * GeminiMetadata record
 * @author cjohn
 */
@Slf4j
@Service
@AllArgsConstructor
public class DataciteService {
    private final static String DATACITE_API = "https://mds.datacite.org";
    private final String doiPrefix;
    private final String publisher;
    private final String username;
    private final String password;
    private final DocumentIdentifierService identifierService;
    private final Template dataciteRequest;
    private final RestTemplate rest = new RestTemplate();

    @Autowired
    public DataciteService(
        @Qualifier("doiPrefix") String doiPrefix,
        @Qualifier("publisher") String publisher,
        @Qualifier("doiUsername") String username,
        @Qualifier("doiPassword") String password,
        DocumentIdentifierService identifierService,
        Configuration configuration
    ) throws IOException {
        this.doiPrefix = doiPrefix;
        this.publisher = publisher;
        this.username = username;
        this.password = password;
        this.identifierService = identifierService;
        this.dataciteRequest = configuration.getTemplate("/datacite/datacite.xml.tpl");
    }

    /**
     * Contacts the DATACITE rest api and uploads a datacite requests and then
     * gets the that request minted
     * @param document
     * @return
     * @throws DataciteException 
     */
    public ResourceIdentifier generateDoi(GeminiDocument document) throws DataciteException {
        ResourceIdentifier doi = updateDoiMetadata(document);
        mintDoiRequest(document);
        return doi;
    }
    
    /**
     * Grab the current metadata document from the datacite api. If this gemini
     * document does not have a doi, then return null
     * @param document
     * @return string containing a doi metadata request, or null if nothing there
     */
    public String getDoiMetadata(GeminiDocument document) {
        String doi = getDoi(document);
        if (doi != null) {
            try {
                return rest.exchange(
                        DATACITE_API + "/metadata/{doi}", 
                        HttpMethod.GET, 
                        new HttpEntity<>(getBasicAuth()), 
                        String.class, 
                        doi
                ).getBody();
            }
            catch(RestClientException ex) {
                throw new DataciteException("Failed to obtain datacite metadata", ex);
            }
        }
        else {
            return null;
        }
    }
    
    /**
     * Contact the datacite rest api and submit a metadata request. This is only
     * possible if the document is in the correct format
     * @param document to submit a metadata datacite request of
     * @return the generated doi represented as a resource identifier
     */
    public ResourceIdentifier updateDoiMetadata(GeminiDocument document) {
        if(isDatacitable(document)) {
            try {
                HttpHeaders headers = getBasicAuth();
                headers.setContentType(MediaType.valueOf("application/xml;charset=UTF-8"));

                String request = getDatacitationRequest(document);
                rest.postForEntity(DATACITE_API + "/metadata", new HttpEntity<>(request, headers), String.class);

                return ResourceIdentifier
                        .builder()
                        .code(generateDoiString(document))
                        .codeSpace("doi:")
                        .build();
            }
            catch(HttpClientErrorException ex) {
                log.error("Failed to upload doi: {} - {}", generateDoiString(document), ex.getResponseBodyAsString());
                throw new DataciteException("Failed to submit the doi metdata, please review the datacite.xml (is it valid?) then try again", ex);
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
     * Mints a datacite metadata request for a given document.
     * @param document the document which should be doi minted
     */
    public void mintDoiRequest(GeminiDocument document) {
        if(isDataciteMintable(document)) {
            String doi = generateDoiString(document);
            String request = String.format("doi=%s\nurl=%s", doi, identifierService.generateUri(document.getId()));
            log.info("Requesting mint of doi: {}", request);
            try {
                HttpHeaders headers = getBasicAuth();
                headers.setContentType(MediaType.TEXT_PLAIN);

                rest.postForEntity(DATACITE_API + "/doi", new HttpEntity<>(request, headers), String.class);
            }
            catch(HttpClientErrorException ex) {
                log.error("Failed to mint doi: {} - {}", doi, ex.getResponseBodyAsString());
                throw new DataciteException("Minting of the DOI failed, please review the datacite.xml (is it valid?) then try again", ex);
            }
            catch(RestClientException ex) {
                throw new DataciteException("Failed to communicate with the datacite api when trying to mint the doi", ex);
            }
        }
        else {
            throw new DataciteException("This record does not meet the requirements for datacite minting");
        }
    }
        
    /**
     * Determines if the specified gemini document can be datacited by this 
     * datacite service
     * @param document to test if ready for data citation
     * @return true if this GeminiDocument can be submitted to datacite
     */
    public boolean isDatacitable(GeminiDocument document) {
        boolean isPubliclyViewable = Optional.ofNullable(document.getMetadata())
                .map(m -> m.isPubliclyViewable(Permission.VIEW))
                .orElse(false);
        boolean hasNonEmptyTitle = !Strings.isNullOrEmpty(document.getTitle());
        boolean hasAuthor = Optional.ofNullable(document.getResponsibleParties())
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch((p) -> "author".equals(p.getRole()));
        boolean hasCorrectPublisher = Optional.ofNullable(document.getResponsibleParties())
                .orElse(Collections.emptyList())
                .stream()
                .filter((p) -> "publisher".equals(p.getRole()))
                .anyMatch((p) -> publisher.equals(p.getOrganisationName()));
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
     * @param document
     * @return true if the GeminiDocument has already been submitted for a doi 
     */
    public boolean isDatacited(GeminiDocument document) {
        return isDatacitable(document) && hasDoi(document);
    }
    
    /**
     * Determine if this geminidocument can be submitted for a fresh doi. This 
     * means it means the doi requirements and doesn't already have a doi
     * @param document
     * @return true if the GeminiDocument can be submitted for a doi
     */
    public boolean isDataciteMintable(GeminiDocument document) {
        return isDatacitable(document) && !hasDoi(document);
    }
    
    private boolean hasDoi(GeminiDocument document) {
        return getDoi(document) != null;
    }
    
    private String getDoi(GeminiDocument document) {
        return Optional.ofNullable(document.getResourceIdentifiers())
                .orElse(Collections.emptyList())
                .stream()
                .filter((i) -> i.getCodeSpace().equals("doi:"))
                .filter((i) -> i.getCode().startsWith(doiPrefix))
                .map(ResourceIdentifier::getCode)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Process the datacitation request template for the given document and 
     * return the request as a string.
     * @param document to get the prepare a datacitation request for
     * @return an xml datacite request
     */
    public String getDatacitationRequest(GeminiDocument document) {
        try {
            String doi = generateDoiString(document);
            Map<String, Object> data = new HashMap<>();
            data.put("doc", document);
            data.put("resourceType", getDataciteResourceType(document));
            data.put("doi", doi);
            return FreeMarkerTemplateUtils.processTemplateIntoString(dataciteRequest, data);
        }
        catch(IOException | TemplateException ex) {
            throw new DataciteException(ex);
        }
    }
        
    private String getDataciteResourceType(MetadataDocument document) {
        switch(document.getType()) {
            case "nonGeographicDataset": return "Dataset";
            case "dataset":              return "Dataset";
            case "application":          return "Other";
            case "model":                return "Model";
            case "service":              return "Service";
            case "software":             return "Software";
            default:                     return null;
        }
    }
    
    private String generateDoiString(GeminiDocument document) {
        return doiPrefix + document.getId();
    }
    
    protected HttpHeaders getBasicAuth() {
        String plainCreds = username + ':' + password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }
}
