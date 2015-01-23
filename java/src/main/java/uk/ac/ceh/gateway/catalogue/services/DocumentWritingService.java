package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.io.OutputStream;

public interface DocumentWritingService<T> {
    void write(T entity, OutputStream stream) throws IOException;
}