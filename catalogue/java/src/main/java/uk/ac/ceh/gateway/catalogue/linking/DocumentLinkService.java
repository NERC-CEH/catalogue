package uk.ac.ceh.gateway.catalogue.linking;

import java.util.List;

public interface DocumentLinkService {
    /**
     * Rebuild links for all documents
     * @throws DocumentLinkingException 
     */
    void rebuildLinks() throws DocumentLinkingException;
    
    /**
     * Checks if any links are present inside the document link service
     * @return true if no links are present, otherwise false
     */
    boolean isEmpty();
    
    /**
     * Link datasets and services
     * @param fileIdentifiers
     * @param revision
     * @throws DocumentLinkingException 
     */
    void linkDocuments(List<String> fileIdentifiers, String revision) throws DocumentLinkingException;
    
    /**
     * Remove any links associated with the given fileIdentifiers
     * @param fileIdentifiers
     * @throws DocumentLinkingException 
     */
    void unlinkDocuments(List<String> fileIdentifiers) throws DocumentLinkingException;
}