package uk.ac.ceh.gateway.catalogue.model;

public class ExternalResourceFailureException extends RuntimeException {
    static final long serialVersionUID = 1L;

    public ExternalResourceFailureException(String mess) {
        super(mess);
    }

    public ExternalResourceFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
