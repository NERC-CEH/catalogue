package uk.ac.ceh.gateway.catalogue.notSureWhereYet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.gemini.Metadata;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
@Service
public class DefaultDocumentReadingService implements DocumentReadingService<Metadata> {
    private final ObjectMapper mapper;
    
    @Autowired
    public DefaultDocumentReadingService(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public Metadata read(InputStream newInputStream, MediaType contentType) throws IOException, UnknownContentTypeException {
        if(contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            return mapper.readValue(newInputStream, Metadata.class);
        }
        else {
            throw new UnknownContentTypeException("I don't know how to read " + contentType);
        }
    }
    
}
