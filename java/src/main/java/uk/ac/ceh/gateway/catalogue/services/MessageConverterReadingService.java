package uk.ac.ceh.gateway.catalogue.services;

import lombok.ToString;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A Document Reading Service which delegates reading to Springs 
 * HttpMessageConverters
 */
@Slf4j
@ToString
public class MessageConverterReadingService implements DocumentReadingService {
    private final List<HttpMessageConverter<?>> messageConverters;
    
    public MessageConverterReadingService() {
        this(new ArrayList<>());
    }
    
    protected MessageConverterReadingService(List<HttpMessageConverter<?>> messageConverters) {
        this.messageConverters = messageConverters;
        log.info("Creating {}", this);
    }
    
    public MessageConverterReadingService addMessageConverter(HttpMessageConverter<?> converter) {
        messageConverters.add(converter);
        log.info("Adding {}", converter);
        return this;
    }
    
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T read(InputStream inputStream, MediaType contentType, Class<T> clazz) throws IOException, UnknownContentTypeException {
        for(HttpMessageConverter converter: messageConverters) {
            if(converter.canRead(clazz, contentType)) {
                return (T)converter.read(clazz, new DocumentReadingHttpInputMessage(inputStream, contentType));
            }
        }
        throw new UnknownContentTypeException("I don't know how to read " + contentType);
    }
    
    @Value
    private static class DocumentReadingHttpInputMessage implements HttpInputMessage {
        InputStream body;
        MediaType contentType;

        @Override
        public HttpHeaders getHeaders() {
            HttpHeaders toReturn = new HttpHeaders();
            toReturn.setContentType(contentType);
            return toReturn;
        }
    }
}
