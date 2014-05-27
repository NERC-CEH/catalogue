package uk.ac.ceh.gateway.catalogue.services;

/**
 *
 * @author cjohn
 */
public interface DocumentBundleService<D,I,B> {  
    B bundle(D document, I info);
}
