package uk.ac.ceh.gateway.catalogue.services;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

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
        return hostUrl + "/maps/" + id + "?";
    }
    
    /**
     * Determine if the specified metadata document can be used to set up a map
     * service
     * @param document to check if this document can define a map server service
     * @return if the supplied document can create a map service document 
     */
    public boolean isMapServiceHostable(MetadataDocument document) {
        if(document instanceof GeminiDocument) {
            return ((GeminiDocument)document).getMapDataDefinition() != null;
        }
        return false;
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
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(wmsUrl).build();
        List<String> pathSegments = uri.getPathSegments();
        if(wmsUrl.startsWith(hostUrl + "/maps") && pathSegments.size() == 2) {
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
