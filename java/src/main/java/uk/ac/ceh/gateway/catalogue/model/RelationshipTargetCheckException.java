package uk.ac.ceh.gateway.catalogue.model;

/**
 * Thrown when a save adds a relationship whose target could not be checked because the datastore
 * could not be read, as opposed to the target being missing, hidden from the user or of the wrong
 * type (which is an {@link InvalidRelationshipTargetException}). A read fault says nothing about
 * the target, so reporting it as one of those would be both wrong and unhelpful: it is temporary,
 * and the save should simply be tried again (dri-one #439).
 */
public class RelationshipTargetCheckException extends RuntimeException {
    static final long serialVersionUID = 1L;
    public RelationshipTargetCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}
