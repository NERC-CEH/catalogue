package uk.ac.ceh.gateway.catalogue.services;

import org.springframework.http.MediaType;

/**
 *
 * @author cjohn
 */
public interface DocumentInfoFactory<D, I> {
    I createInfo(D document, MediaType contentType);
}
