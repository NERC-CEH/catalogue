package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 * A Document Reading Service which delegates reading to Springs 
 * HttpMessageConverters
 * @author cjohn
 * @param <T> The type that is expected to be read by this Reading Service
 */
public class MessageConverterReadingService<T> implements DocumentReadingService<T> {
    private final List<HttpMessageConverter<?>> messageConverters;
    private final Class<T> clazz;
    
    public MessageConverterReadingService(Class<T> clazz) {
        this(clazz, new ArrayList<>());
    }
    
    protected MessageConverterReadingService(Class<T> clazz, List<HttpMessageConverter<?>> messageConverters) {
        this.messageConverters = messageConverters;
        this.clazz = clazz;
    }
    
    public MessageConverterReadingService addMessageConverter(HttpMessageConverter<?> converter) {
        messageConverters.add(converter);
        return this;
    }
    
    @Override
    public T read(InputStream inputStream, MediaType contentType) throws IOException, UnknownContentTypeException {
        for(HttpMessageConverter converter: messageConverters) {
            if(converter.canRead(clazz, contentType)) {
                return (T)converter.read(clazz, new DocumentReadingHttpInputMessage(inputStream, contentType));
            }
        }
        throw new UnknownContentTypeException("I don't know how to read " + contentType);
    }
    
    @Data
    private static class DocumentReadingHttpInputMessage implements HttpInputMessage {
        private final InputStream body;
        private final MediaType contentType;

        @Override
        public HttpHeaders getHeaders() {
            HttpHeaders toReturn = new HttpHeaders();
            toReturn.setContentType(contentType);
            return toReturn;
        }
    }
}
