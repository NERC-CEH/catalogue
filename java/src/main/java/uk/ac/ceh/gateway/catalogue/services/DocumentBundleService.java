package uk.ac.ceh.gateway.catalogue.services;

/**
 *
 * @author cjohn
 */
public interface DocumentBundleService<D,I> {  
    D bundle(D document, I info);
}
