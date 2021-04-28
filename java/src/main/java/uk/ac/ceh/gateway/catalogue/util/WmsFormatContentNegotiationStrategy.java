package uk.ac.ceh.gateway.catalogue.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Define a content negotiation strategy which will read literal media type values 
 * from a specified query parameter and use this as the preferred mediatype.
 * 
 * This logic defined here is designed to support WMS style requests which state
 * that 'Parameter names shall not be case sensitive'
 * @see http://cite.opengeospatial.org/OGCTestData/wms/1.1.1/spec/wms1.1.1.html
 */
@Data
@AllArgsConstructor
public class WmsFormatContentNegotiationStrategy implements ContentNegotiationStrategy {
    private String parameter;
    
    @Override
    public List<MediaType> resolveMediaTypes(NativeWebRequest webRequest) throws HttpMediaTypeNotAcceptableException {
        Optional<Map.Entry<String, String[]>> mediaType = webRequest
                .getParameterMap()
                .entrySet()
                .stream()
                .filter((e) -> e.getKey().equalsIgnoreCase(parameter))
                .findFirst();

        if(mediaType.isPresent()) {
            try {
                return MediaType.parseMediaTypes(mediaType.get().getValue()[0]);
            }
            catch(InvalidMediaTypeException ignored) {}
        }
        return Collections.emptyList();
    }
}