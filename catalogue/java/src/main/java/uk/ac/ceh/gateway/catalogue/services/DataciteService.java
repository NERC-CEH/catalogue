package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.base.Strings;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.DataciteException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Permission;

/**
 * A service which interacts with the datacite rest api to obtain a DOI for a 
 * GeminiMetadata record
 * @author cjohn
 */
@Data
@Slf4j
public class DataciteService {
    private final static String DATACITE_API = "https://mds.datacite.org";
    private final String doiPrefix;
    private final String publisher;
    private final String username;
    private final String password;
    private final Template dataciteRequest;
    
    /**
     * For the given gemini document, submit a request to datacite to get a DOI
     * If obtained successfully, add this as a resource identifier to the 
     * document and then return.
     * @param document Gemini Document to submit to datacite
     * @return GeminiDocument with doi as a resourceIdentifier
     */
    public ResourceIdentifier generateDoi(GeminiDocument document) throws DataciteException {
        uploadDoiMetadata(document);
        mintDoiRequest(document);
        
        return ResourceIdentifier
            .builder()
            .code(getDoi(document))
            .codeSpace("doi:")
            .build();
    }
    
    public String uploadDoiMetadata(GeminiDocument document) {
        String doi = getDoi(document);
        Map<String, Object> data = new HashMap<>();
        data.put("doc", document);
        data.put("resourceType", getDataciteResourceType(document));
        data.put("doi", doi);
        try {
            String request = FreeMarkerTemplateUtils.processTemplateIntoString(dataciteRequest, data);

            HttpHeaders headers = getBasicAuth();
            headers.add("Content", "application/xml");
            
            ResponseEntity<String> response = new RestTemplate()
                    .postForEntity(DATACITE_API + "/metadata", new HttpEntity<>(request, headers), String.class);
            
            return response.getBody();
        }
        catch(IOException | TemplateException ex) {
            throw new DataciteException(ex);
        }
        catch(HttpClientErrorException ex) {
            log.error("Failed to upload doi: {} - {}", doi, ex.getResponseBodyAsString());
            throw new DataciteException("Failed to mint the doi", ex);
        }
        catch(RestClientException ex) {
            throw new DataciteException("The datacite service failed to upload a record", ex);
        }
    }
    
    public String mintDoiRequest(GeminiDocument document) {
        String doi = getDoi(document);
        String request = String.format("doi=%s\nurl=%s", doi, document.getUri().toString());
        log.info("Requesting mint of doi: {}", request);
        try {
            HttpHeaders headers = getBasicAuth();
            headers.add("Content", "text/plain");
            
            ResponseEntity<String> response = new RestTemplate()
                    .postForEntity(DATACITE_API + "/doi", new HttpEntity<>(request, headers), String.class);
            
            return response.getBody();
        }
        catch(HttpClientErrorException ex) {
            log.error("Failed to mint doi: {} - {}", doi, ex.getResponseBodyAsString());
            throw new DataciteException("Failed to mint the doi", ex);
        }
        catch(RestClientException ex) {
            throw new DataciteException("Failed to mint the doi", ex);
        }
    }
        
    /**
     * Determines if the specified gemini document can be datacited by this 
     * datacite service
     * @param document to test if ready for data citation
     * @return true if this GeminiDocument can be submitted to datacite
     */
    public boolean isDatacitable(GeminiDocument document) {
        boolean hasDoi = Optional.ofNullable(document.getResourceIdentifiers())
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch((i) -> i.getCodeSpace().equals("doi:"));
        boolean hasAuthor = Optional.ofNullable(document.getResponsibleParties())
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch((p) -> p.getRole().equals("author"));
        boolean hasCorrectPublisher = Optional.ofNullable(document.getResponsibleParties())
                .orElse(Collections.emptyList())
                .stream()
                .filter((p) -> "publisher".equals(p.getRole()))
                .anyMatch((p) -> publisher.equals(p.getOrganisationName()));
        
        return document.getMetadata().isPubliclyViewable(Permission.VIEW)
                && !Strings.isNullOrEmpty(document.getTitle())
                && !hasDoi
                && hasAuthor
                && hasCorrectPublisher;
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
    
    private String getDoi(GeminiDocument document) {
        return doiPrefix + document.getId();
    }
    
    private HttpHeaders getBasicAuth() {
        String plainCreds = username + ':' + password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }
}
