package uk.ac.ceh.gateway.catalogue.model;

public class ResourceIdentifierExistsException extends RuntimeException {
    static final long serialVersionUID = 1L;
    public ResourceIdentifierExistsException(String message) {
        super(message);
    }
}
