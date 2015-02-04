package uk.ac.ceh.gateway.catalogue.services;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.ExternalResourceFailureException;
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
    
    @Cacheable("capabilities")
    public WmsCapabilities getWmsCapabilities(OnlineResource resource) {
        if(resource.getType().equals(OnlineResource.Type.WMS_GET_CAPABILITIES)) {
            try {
                return rest.getForObject(resource.getUrl(), WmsCapabilities.class);
            }
            catch(IllegalArgumentException | RestClientException re) {
                throw new ExternalResourceFailureException("Failed to obtain a get capabilities from the given online resource");
            }
        }
        else {
            throw new NotAGetCapabilitiesResourceException("The specified online resource does not represent a get capabilities");
        }
    }
}
