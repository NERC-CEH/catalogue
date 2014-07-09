package uk.ac.ceh.gateway.catalogue.linking;

import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.elements.ResourceIdentifier;

@Value
public class Metadata {
    private final String fileIdentifier, title, resourceIdentifier;
    
    public Metadata(GeminiDocument document) {
        fileIdentifier = nullToEmpty(document.getId());
        title = nullToEmpty(document.getTitle());
        this.resourceIdentifier = extractInternalIdentifier(document);
    }
    
    private String extractInternalIdentifier(GeminiDocument document) {
        if (document.getResourceIdentifiers() != null) {
            for (ResourceIdentifier identifier : document.getResourceIdentifiers()) {
                if (identifier.isInternal()) {
                    return identifier.getCoupleResource();
                } 
            }
        }
        return "";
    }
}