package uk.ac.ceh.gateway.catalogue.model;

/**
 *
 * @author Christopher Johnson
 */
public class DocumentAlreadyExistsException extends RuntimeException {
    public DocumentAlreadyExistsException() {
        super();
    }
    
    public DocumentAlreadyExistsException(String mess) {
        super(mess);
    }
    
    public DocumentAlreadyExistsException(Throwable cause) {
        super(cause);
    }
    
    public DocumentAlreadyExistsException(String mess, Throwable cause) {
        super(mess, cause);
    }
}