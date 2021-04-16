package uk.ac.ceh.gateway.catalogue.util;

import org.springframework.http.MediaType;
import org.springframework.web.accept.ParameterContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Map;

/**
 * The following is a Forgiving version of the ParameterContentNegotiationStrategy
 * in that if a format parameter is specified which is not known, the application
 * will ignore it and not use it for content negotiation.
 * 
 * This is useful when proxying wms services, as it allows the format parameter 
 * to just pass through.
 */
public class ForgivingParameterContentNegotiationStrategy extends ParameterContentNegotiationStrategy {
    
    public ForgivingParameterContentNegotiationStrategy(Map<String, MediaType> mediaTypes) {
        super(mediaTypes);
    }
    
    @Override
    protected MediaType handleNoMatch(NativeWebRequest request, String key) {
        return null;
    }
}
