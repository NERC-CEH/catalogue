package uk.ac.ceh.gateway.catalogue.document.reading;

import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.document.UnknownContentTypeException;

import java.io.IOException;
import java.io.InputStream;

public interface DocumentReadingService {
    <T> T read(InputStream inputStream, MediaType contentType, Class<T> clazz) throws UnknownContentTypeException, IOException;
}
