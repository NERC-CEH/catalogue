package uk.ac.ceh.gateway.catalogue.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataWriter;

/**
 *
 * @author cjohn
 */
@Service
public class JsonObjectMapperService {
    private final ObjectMapper mapper;
    
    @Autowired
    public JsonObjectMapperService(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    public <T> T read(InputStream stream, Class<T> type) throws IOException {
        return mapper.readValue(stream, type);
    }
    
    public DataWriter of(final Object content) {
        return (OutputStream out) -> {
            try {
                mapper.writeValue(out, content);
            } catch (IOException ex) {
                throw new DataRepositoryException(ex);
            }
        };
    }
}
