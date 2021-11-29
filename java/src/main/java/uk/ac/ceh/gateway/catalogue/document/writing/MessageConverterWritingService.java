package uk.ac.ceh.gateway.catalogue.document.writing;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import uk.ac.ceh.gateway.catalogue.document.UnknownContentTypeException;

import java.io.*;
import java.util.List;

/**
 * A Document Writing service which will write a given document to an inputStream
 * of the requested media type
 */
@Slf4j
@ToString(exclude = "messageConverters")
public class MessageConverterWritingService implements DocumentWritingService {
    private final List<HttpMessageConverter<?>> messageConverters;

    public MessageConverterWritingService(
            List<HttpMessageConverter<?>> messageConverters
    ) {
        this.messageConverters = messageConverters;
        log.info("Creating {}", this);
    }

    public MessageConverterWritingService addMessageConverter(HttpMessageConverter<?> converter) {
        messageConverters.add(converter);
        log.info("Adding {}", converter);
        return this;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void write(T document, MediaType contentType, OutputStream output) throws IOException, UnknownContentTypeException {
        for(HttpMessageConverter converter: messageConverters) {
            log.debug("Supported Media Types: {}", converter.getSupportedMediaTypes());
            log.debug("Media Type requested: {}, for {}", contentType, document.getClass());
            if(converter.canWrite(document.getClass(), contentType)) {
                converter.write(document, contentType, new HttpOutputMessageWrapper(output));
                return;
            }
            log.debug("Cannot write for: {}", contentType);
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
