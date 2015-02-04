package uk.ac.ceh.gateway.catalogue.linking;

import java.util.Optional;
import java.util.Set;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Link;

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
     * @throws DocumentLinkingException 
     */
    void linkDocuments(Set<String> fileIdentifiers) throws DocumentLinkingException;
    
    /**
     * Get the links associated with this Gemini Document.
     * 
     * For a Service return links to Datasets.
     * For a Dataset return links to Services.
     * 
     * @param document to find links for
     * @param urlFragment to use to create link url from (expects to have context/documents/ or context/history/ in path)
     * @return a set of Links
     */
    Set<Link> getLinks(GeminiDocument document, String urlFragment);
    
    /**
     * Get link to parent of this Gemini Document.
     * 
     * @param document to find parent of
     * @param urlFragment to use to create link url from (expects to have context/documents/ or context/history/ in path)
     * @return a Link to the parent Metadata
     */
    Optional<Link> getParent(GeminiDocument document, String urlFragment);
    
    /**
     * Get links to children of this Gemini Document.
     * 
     * @param document to find children of
     * @param urlFragment to use to create link url from (expects to have context/documents/ or context/history/ in path)
     * @return a set of Links to the child Metadata
     */
    Set<Link> getChildren(GeminiDocument document, String urlFragment);
    
    /**
     * 
     * @param document to find revision
     * @param urlFragment to use to create link url from (expects to have context/documents/ or context/history/ in path)
     * @return a Link to the revised Metadata
     */
    Optional<Link> getRevised(GeminiDocument document, String urlFragment);
    
    Optional<Link> getRevisionOf(GeminiDocument document, String urlFragment);
}