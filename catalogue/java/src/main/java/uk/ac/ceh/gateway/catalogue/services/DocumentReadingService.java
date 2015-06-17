package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.http.MediaType;

/**
 *
 * @author cjohn
 */
public interface DocumentReadingService {
    <T> T read(InputStream inputStream, MediaType contentType, Class<T> clazz) throws UnknownContentTypeException, IOException;
}
