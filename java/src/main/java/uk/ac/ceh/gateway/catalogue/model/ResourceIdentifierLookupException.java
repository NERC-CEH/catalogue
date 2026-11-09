package uk.ac.ceh.gateway.catalogue.model;

/**
 * Thrown when the search index could not be asked which records hold a resource identifier.
 * <p>
 * That says nothing about whether the identifier exists, so it must not be reported as "no match":
 * the {@code /id/{codespace}/{code}} resolver would redirect to a URL that 404s, and the save-time
 * uniqueness check would let a duplicate through unchecked. It is temporary, so the caller should
 * try again (dri-one #522; the same reasoning as {@link RelationshipTargetCheckException}).
 * <p>
 * The message is returned to the client, so it must not carry Solr details; the cause is logged
 * where it is caught.
 */
public class ResourceIdentifierLookupException extends RuntimeException {
    static final long serialVersionUID = 1L;
    public ResourceIdentifierLookupException(String message, Throwable cause) {
        super(message, cause);
    }
}
