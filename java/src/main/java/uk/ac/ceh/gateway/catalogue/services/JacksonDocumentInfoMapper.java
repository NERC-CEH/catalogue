package uk.ac.ceh.gateway.catalogue.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author cjohn
 */
public class JacksonDocumentInfoMapper<T> implements DocumentInfoMapper<T> {
    private final ObjectMapper mapper;
    private final Class<T> clazz;
    
    public JacksonDocumentInfoMapper(ObjectMapper mapper, Class<T> clazz) {
        this.mapper = mapper;
        this.clazz = clazz;
    }

    @Override
    public void writeInfo(T info, OutputStream stream) throws IOException {
        try (OutputStream out = stream) {
            mapper.writeValue(out, info);
        }
    }

    @Override
    public T readInfo(InputStream readFrom) throws IOException {
        try (InputStream in = readFrom) {
            return mapper.readValue(in, clazz);
        }
    }
}
