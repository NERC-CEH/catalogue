package uk.ac.ceh.gateway.catalogue.controllers;

public class PermissionDeniedException extends RuntimeException {

    public PermissionDeniedException(String message) {
        super(message);
    }

    public PermissionDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
    
}