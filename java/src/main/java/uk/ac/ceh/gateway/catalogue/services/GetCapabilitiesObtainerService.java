package uk.ac.ceh.gateway.catalogue.services;

import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.NotAGetCapabilitiesResourceException;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;

/**
 *
 * @author cjohn
 */
public class GetCapabilitiesObtainerService {
    private final RestTemplate rest;
    
    public GetCapabilitiesObtainerService(RestTemplate rest) {
        this.rest = rest;
    }
    
    public WmsCapabilities getWmsCapabilities(OnlineResource resource) {
        if(resource.getType().equals(OnlineResource.Type.GET_CAPABILITIES)) {
            return rest.getForObject(resource.getUrl(), WmsCapabilities.class);
        }
        else {
            throw new NotAGetCapabilitiesResourceException("The specified online resource does not represent a get capabilities");
        }
    }
}
