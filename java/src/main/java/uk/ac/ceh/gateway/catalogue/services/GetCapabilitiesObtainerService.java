package uk.ac.ceh.gateway.catalogue.services;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.ExternalResourceFailureException;
import uk.ac.ceh.gateway.catalogue.model.NotAGetCapabilitiesResourceException;
import uk.ac.ceh.gateway.catalogue.ogc.WmsCapabilities;

import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.String.format;

@Slf4j
@ToString
public class GetCapabilitiesObtainerService {
    private final RestTemplate rest;
    private final MapServerDetailsService mapServerDetailsService;

    public GetCapabilitiesObtainerService(RestTemplate rest, MapServerDetailsService mapServerDetailsService) {
        this.rest = rest;
        this.mapServerDetailsService = mapServerDetailsService;
        log.info("Creating {}", this);
    }

    @Cacheable("capabilities")
    public WmsCapabilities getWmsCapabilities(OnlineResource resource) {
        if(resource.getType().equals(OnlineResource.Type.WMS_GET_CAPABILITIES)) {
            try {
                String rewritten = mapServerDetailsService.rewriteToLocalWmsRequest(resource.getUrl());
                return rest.getForObject(new URI(rewritten), WmsCapabilities.class);
            }
            catch(URISyntaxException | IllegalArgumentException | RestClientException re) {
                throw new ExternalResourceFailureException(
                    format("Failed to obtain capabilities from: %s", resource.getUrl()),
                    re
                );
            }
        }
        else {
            throw new NotAGetCapabilitiesResourceException("The specified online resource does not represent a get capabilities");
        }
    }
}
