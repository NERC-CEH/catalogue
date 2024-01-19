package uk.ac.ceh.gateway.catalogue.model;

public class PublicationServiceException extends RuntimeException {
    static final long serialVersionUID = 1L;

    public PublicationServiceException() {
        super();
    }

    public PublicationServiceException(String mess) {
        super(mess);
    }

    public PublicationServiceException(Throwable cause) {
        super(cause);
    }

    public PublicationServiceException(String mess, Throwable cause) {
        super(mess, cause);
    }
}
