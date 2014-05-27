package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.springframework.http.MediaType;

/**
 *
 * @author cjohn
 */
public interface DocumentInfoMapper<I, D> {
    I createInfo(D document, MediaType contentType);
    
    void writeInfo(I info, OutputStream stream) throws IOException;
    
    I readInfo(InputStream in) throws IOException;
}
