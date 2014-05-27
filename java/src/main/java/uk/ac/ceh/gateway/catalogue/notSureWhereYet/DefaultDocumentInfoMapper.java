package uk.ac.ceh.gateway.catalogue.notSureWhereYet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.gemini.Metadata;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;

/**
 *
 * @author cjohn
 */
@Service
public class DefaultDocumentInfoMapper implements DocumentInfoMapper<MetadataInfo, Metadata> {
    private final ObjectMapper mapper;
    
    @Autowired
    public DefaultDocumentInfoMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public MetadataInfo createInfo(Metadata document, MediaType contentType) {
        MetadataInfo toReturn = new MetadataInfo();
        toReturn.setRawType(contentType.toString());
        return toReturn;
    }

    @Override
    public void writeInfo(MetadataInfo info, OutputStream stream) throws IOException {
        mapper.writeValue(stream, info);
    }

    @Override
    public MetadataInfo readInfo(InputStream in) throws IOException {
        try {
            return mapper.readValue(in, MetadataInfo.class);
        }
        finally {
            in.close();
        }
    }
}
