package uk.ac.ceh.gateway.catalogue.services;

import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The following service is a 'helper' which produces text which is useful in
 * the creation of map server mapfiles.
 * @author cjohn
 */
@AllArgsConstructor
public class MapServerDetailsService {
    private final String hostUrl;
    
    /**
     * For the given document, return the potential wms endpoint where the 
     * service could be hosted.
     * @param id of the document to generate a wms endpoint for
     * @return wms url
     */
    public String getWmsUrl(String id) {
        return hostUrl + "/documents/" + id + "/wms?";
    }
    
    /**
     * Takes the supplied wms url and determines if the request contacts an 
     * endpoint of this application. If it does, then the request is rewritten
     * to contact the hosted mapserver instance directly. This avoids a double
     * proxying of the request by contacting the hosted mapserver instance 
     * directly.
     * @param wmsUrl
     * @return 
     */
    public String rewriteToLocalWmsRequest(String wmsUrl) {
        List<String> hosts = Arrays.asList(UriComponentsBuilder.fromHttpUrl(hostUrl).build().getHost());
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(wmsUrl).build();
        
        List<String> pathSegments = uri.getPathSegments();
        if(     hosts.contains(uri.getHost()) &&
                pathSegments.size() == 3 &&
                pathSegments.get(0).equals("documents") &&
                pathSegments.get(2).equals("wms")) {
            
            return getLocalWMSRequest(pathSegments.get(1), uri.getQuery());
        }
        return wmsUrl;
    }
    
    /**
     * Generate a wms url which contacts the catalogues mapserver instance for 
     * the given id and query string.
     * @param id of the wms service to call
     * @param query string containing wms request parameters
     * @return a wms url to request
     */
    public String getLocalWMSRequest(String id, String query) {
        return UriComponentsBuilder
                .fromHttpUrl("http://mapserver/{id}")
                .query(query)
                .buildAndExpand(id)
                .toUriString();
    }
}
