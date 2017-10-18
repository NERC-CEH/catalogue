package uk.ac.ceh.gateway.catalogue.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
@AllArgsConstructor
public class JacksonDocumentInfoMapper<T> implements DocumentInfoMapper<T> {
    private final ObjectMapper mapper;
    private final Class<T> clazz;

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
