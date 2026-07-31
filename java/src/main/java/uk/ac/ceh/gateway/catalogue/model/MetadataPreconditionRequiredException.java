package uk.ac.ceh.gateway.catalogue.model;

/**
 * Thrown when a metadata update is attempted without the required If-Match precondition header, so no
 * optimistic-lock check can be performed. Maps to HTTP 428 Precondition Required.
 */
public class MetadataPreconditionRequiredException extends RuntimeException {
    static final long serialVersionUID = 1L;
    public MetadataPreconditionRequiredException(String message) {
        super(message);
    }
}
