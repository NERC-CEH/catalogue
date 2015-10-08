package uk.ac.ceh.gateway.catalogue.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;

/**
 * Queries the shortdoi.org service to obtain a shortened version of a doi
 * @author cjohn
 */
public class ShortDoiService {
    
    public ResourceIdentifier shortenDoi(String doi) throws RestClientException {
        ShortDoiResponse response = new RestTemplate()
                .getForObject("http://shortdoi.org/{doi}", ShortDoiResponse.class, doi);
        
        return ResourceIdentifier
                .builder()
                .code(response.getShortDoi())
                .codeSpace("doi:")
                .build();
    }
    
    @Data
    private static class ShortDoiResponse {
        private @JsonProperty("DOI") String doi;
        private @JsonProperty("IsNew") boolean isNew;
        private @JsonProperty("ShortDOI") String shortDoi;
    }
}
