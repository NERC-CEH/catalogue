package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.springframework.http.MediaType;

public interface DocumentWritingService {
    <T> void write(T document, MediaType contentType, OutputStream output) throws IOException, UnknownContentTypeException;
    <T> InputStream write(T document, MediaType contentType) throws IOException, UnknownContentTypeException;
}