package uk.ac.ceh.gateway.catalogue.services;

/**
 *
 * @author cjohn
 */
public class UnknownContentTypeException extends Exception {
    public UnknownContentTypeException() {
        super();
    }
    
    public UnknownContentTypeException(String mess) {
        super(mess);
    }
    
    public UnknownContentTypeException(Throwable cause) {
        super(cause);
    }
    
    public UnknownContentTypeException(String mess, Throwable cause) {
        super(mess, cause);
    }    
}
