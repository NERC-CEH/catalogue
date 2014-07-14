package uk.ac.ceh.gateway.catalogue.linking;

import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.experimental.Builder;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.elements.ResourceIdentifier;

@Value
public class Metadata {
    private final String fileIdentifier, title, resourceIdentifier;
    
    public Metadata(GeminiDocument document) {
        this.fileIdentifier = nullToEmpty(document.getId());
        this.title = nullToEmpty(document.getTitle());
        this.resourceIdentifier = extractInternalIdentifier(document);
    }
    
    @Builder
    private Metadata(String fileIdentifier, String title, String resourceIdentifier) {
        this.fileIdentifier = nullToEmpty(fileIdentifier);
        this.title = nullToEmpty(title);
        this.resourceIdentifier = nullToEmpty(resourceIdentifier);
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