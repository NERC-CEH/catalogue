package uk.ac.ceh.gateway.catalogue.util;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.MAPSERVER_GML_VALUE;
import uk.ac.ceh.gateway.catalogue.model.MapServerException;

/**
 * If the response from the MapServer does not use the GML media type, then we
 * can assume that the request is in error.
 * @author cjohn
 */
public class MapServerGetFeatureInfoErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return !MediaType.parseMediaType(MAPSERVER_GML_VALUE)
                         .isCompatibleWith(response.getHeaders().getContentType());
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        String body = IOUtils.toString(response.getBody(), "UTF-8");
        throw new MapServerException(body, response.getStatusCode(), response.getHeaders().getContentType());
    }
}
