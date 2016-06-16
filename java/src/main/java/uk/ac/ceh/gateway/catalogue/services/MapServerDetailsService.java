package uk.ac.ceh.gateway.catalogue.services;

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
        String host = UriComponentsBuilder.fromHttpUrl(hostUrl).build().getHost();
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(wmsUrl).build();
        
        List<String> pathSegments = uri.getPathSegments();
        if(uri.getHost().startsWith(host) && 
                pathSegments.size() == 3 &&
                pathSegments.get(0).equals("documents") &&
                pathSegments.get(2).equals("wms")) {
            
            return UriComponentsBuilder
                    .fromHttpUrl("http://mapserver/{id}")
                    .query(uri.getQuery())
                    .buildAndExpand(pathSegments.get(1))
                    .toUriString();
        }
        return wmsUrl;
    }
}
