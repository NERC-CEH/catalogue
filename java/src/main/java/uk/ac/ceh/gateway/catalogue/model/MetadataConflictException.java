package uk.ac.ceh.gateway.catalogue.model;

import lombok.Getter;

import java.io.Serial;

/**
 * Thrown when a metadata save is rejected because the document has changed since the editor loaded it
 * (optimistic-lock conflict). Carries the submitted-but-unsaved document so the caller can echo it back
 * to the client, preserving the user's in-progress edit. Maps to HTTP 409.
 */
@Getter
public class MetadataConflictException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    private final transient MetadataDocument submittedDocument;

    public MetadataConflictException(String message, MetadataDocument submittedDocument) {
        super(message);
        this.submittedDocument = submittedDocument;
    }

}
