package uk.ac.ceh.gateway.catalogue.model;

/**
 * Thrown when a document being saved adds a relationship whose target is not a kind of record
 * that relationship can point at, e.g. a {@code doo:utilises} link to something other than a
 * monitoring facility or network (dri-one #404). The editor's target search already prevents
 * this, so in practice it catches saves made through the API or from hand-edited JSON.
 */
public class InvalidRelationshipTargetException extends RuntimeException {
    static final long serialVersionUID = 1L;
    public InvalidRelationshipTargetException(String message) {
        super(message);
    }
}
