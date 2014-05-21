package uk.ac.ceh.gateway.catalogue.services;

/**
 * I think this should be the basis for a new component in the java commons
 * @author cjohn
 */
public class URLLinkingService {
    public String getEndpointForDocument(String document) {
        return document;
    }
    
    public <T> T getInContext(T document, String revision) {
        return document;
    }
}
