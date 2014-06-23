package uk.ac.ceh.gateway.catalogue.publication;

public class PublicationException extends RuntimeException {
    public PublicationException() {
        super();
    }
    
    public PublicationException(String mess) {
        super(mess);
    }
    
    public PublicationException(Throwable cause) {
        super(cause);
    }
    
    public PublicationException(String mess, Throwable cause) {
        super(mess, cause);
    }
}