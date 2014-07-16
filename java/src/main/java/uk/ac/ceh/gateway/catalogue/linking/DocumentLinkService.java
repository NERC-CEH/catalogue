package uk.ac.ceh.gateway.catalogue.linking;

import java.util.Set;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.elements.Link;

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
     * @param builder to use to create link url from (expects to have context/documents/{fileIdentifier} in path)
     * @return a set of Links
     */
    Set<Link> getLinks(GeminiDocument document, UriComponentsBuilder builder);

}