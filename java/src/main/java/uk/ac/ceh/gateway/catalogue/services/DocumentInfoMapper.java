package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author cjohn
 */
public interface DocumentInfoMapper<I> {
    
    void writeInfo(I info, OutputStream stream) throws IOException;
    
    I readInfo(InputStream in) throws IOException;
}