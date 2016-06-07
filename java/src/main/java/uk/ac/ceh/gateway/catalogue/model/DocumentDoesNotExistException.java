package uk.ac.ceh.gateway.catalogue.model;

public class DocumentDoesNotExistException extends RuntimeException {
    public DocumentDoesNotExistException() {
        super();
    }
    
    public DocumentDoesNotExistException(String mess) {
        super(mess);
    }
    
    public DocumentDoesNotExistException(Throwable cause) {
        super(cause);
    }
    
    public DocumentDoesNotExistException(String mess, Throwable cause) {
        super(mess, cause);
    }
}