package uk.ac.ceh.gateway.catalogue.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

/**
 * A Document Writing service which will write a given document to an inputstream
 * of the requested mediatype
 * @author cjohn
 */
public class MessageConverterWritingService implements DocumentWritingService {
    private final List<HttpMessageConverter<?>> messageConverters;
    
    public MessageConverterWritingService() {
        this(new ArrayList<>());
    }
    
    public MessageConverterWritingService(List<HttpMessageConverter<?>> messageConverters) {
        this.messageConverters = messageConverters;
    }
    
    public MessageConverterWritingService addMessageConverter(HttpMessageConverter<?> converter) {
        messageConverters.add(converter);
        return this;
    }
    
    @Override
    public <T> void write(T document, MediaType contentType, OutputStream output) throws IOException, UnknownContentTypeException {
        for(HttpMessageConverter converter: messageConverters) {
            if(converter.canWrite(document.getClass(), contentType)) {
                converter.write(document, contentType, new HttpOutputMessageWrapper(output));
            }
        }
        throw new UnknownContentTypeException("I don't know how to read " + contentType);
    }
    
    @Override
    public <T> InputStream write(T document, MediaType contentType) throws IOException, UnknownContentTypeException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        write(document, contentType, buffer);
        return new ByteArrayInputStream(buffer.toByteArray());
    }
    
    @Data
    private static class HttpOutputMessageWrapper implements HttpOutputMessage {
        private HttpHeaders headers = new HttpHeaders();
        private final OutputStream body;
    }
}
