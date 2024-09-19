package uk.ac.ceh.gateway.catalogue.wms;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import uk.ac.ceh.gateway.catalogue.model.MapServerException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.MAPSERVER_GML;

/**
 * If the response from the MapServer does not use the GML media type, then we
 * can assume that the request is in error.
 */
public class MapServerGetFeatureInfoErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) {
        return !MAPSERVER_GML.isCompatibleWith(response.getHeaders().getContentType());
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        val body = IOUtils.toString(response.getBody(), StandardCharsets.UTF_8);
        val httpStatus = HttpStatus.valueOf(response.getStatusCode().value());
        throw new MapServerException(body, httpStatus, response.getHeaders().getContentType());
    }
}
