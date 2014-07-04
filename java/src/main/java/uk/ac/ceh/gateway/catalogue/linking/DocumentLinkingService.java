package uk.ac.ceh.gateway.catalogue.linking;

import java.util.List;

public interface DocumentLinkingService {
    /**
     * Rebuild links for all documents
     * @throws DocumentLinkingException 
     */
    void rebuildLinks() throws DocumentLinkingException;
    
    /**
     * Link datasets and services
     * @param fileIdentifiers
     * @throws DocumentLinkingException 
     */
    void linkDocuments(List<String> fileIdentifiers) throws DocumentLinkingException;

}