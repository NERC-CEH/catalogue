package uk.ac.ceh.gateway.catalogue.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;


@Slf4j
public class JsonDocumentWritingService implements DocumentWritingService<MetadataDocument> {
    private final ObjectMapper mapper;
    
    public JsonDocumentWritingService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void write(MetadataDocument entity, OutputStream stream) throws IOException {
        try (OutputStream out = stream) {
            log.debug("Writing to out: {}", entity);
            mapper.writeValue(out, entity);
        }
    }
}