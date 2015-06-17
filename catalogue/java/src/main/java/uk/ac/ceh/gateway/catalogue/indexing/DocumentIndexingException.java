package uk.ac.ceh.gateway.catalogue.indexing;

/**
 *
 * @author cjohn
 */
public class DocumentIndexingException extends Exception {
    public DocumentIndexingException() {
        super();
    }
    
    public DocumentIndexingException(String mess) {
        super(mess);
    }
    
    public DocumentIndexingException(Throwable cause) {
        super(cause);
    }
    
    public DocumentIndexingException(String mess, Throwable cause) {
        super(mess, cause);
    }    
}
