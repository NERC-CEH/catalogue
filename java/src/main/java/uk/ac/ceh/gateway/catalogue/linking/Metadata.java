package uk.ac.ceh.gateway.catalogue.linking;

import static com.google.common.base.Strings.nullToEmpty;
import java.util.Collections;
import java.util.Optional;
import lombok.Value;
import lombok.experimental.Builder;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;

@Value
public class Metadata {
    private final String fileIdentifier, title, resourceIdentifier, parentIdentifier, revisionOfIdentifier;
    
    public Metadata(GeminiDocument document) {
        this.fileIdentifier = nullToEmpty(document.getId());
        this.title = nullToEmpty(document.getTitle());
        this.resourceIdentifier = extractInternalIdentifier(document);
        this.parentIdentifier = nullToEmpty(document.getParentIdentifier());
        this.revisionOfIdentifier = nullToEmpty(document.getRevisionOfIdentifier());
    }
    
    @Builder
    private Metadata(String fileIdentifier, String title, String resourceIdentifier, String parentIdentifier, String revisionOfIdentifier) {
        this.fileIdentifier = extractFileIdentifier(fileIdentifier);
        this.title = nullToEmpty(title);
        this.resourceIdentifier = nullToEmpty(resourceIdentifier);
        this.parentIdentifier = nullToEmpty(parentIdentifier);
        this.revisionOfIdentifier = nullToEmpty(revisionOfIdentifier);
    }
    
    private String extractInternalIdentifier(GeminiDocument document) {
        return Optional.of(document)
            .map(GeminiDocument::getResourceIdentifiers)
            .orElse(Collections.emptySet())
            .stream()
            .filter(ResourceIdentifier::isInternal)
            .findFirst()
            .map(ResourceIdentifier::getCoupleResource)
            .orElse("");
    }
    
    private String extractFileIdentifier(String fileIdentifier) {
        return Optional.ofNullable(fileIdentifier)
            .orElseThrow(() -> { 
            return new IllegalStateException("Cannot link Metadata document with missing fileIdentifier");
        });
    }
}