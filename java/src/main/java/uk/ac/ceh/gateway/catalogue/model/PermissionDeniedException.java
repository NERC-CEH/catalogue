package uk.ac.ceh.gateway.catalogue.model;

public class PermissionDeniedException extends RuntimeException {

    public PermissionDeniedException() {
        super();
    }

    public PermissionDeniedException(String message) {
        super(message);
    }

    public PermissionDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
    
}