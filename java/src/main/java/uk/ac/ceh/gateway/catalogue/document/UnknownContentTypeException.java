package uk.ac.ceh.gateway.catalogue.document;

public class UnknownContentTypeException extends RuntimeException {
    static final long serialVersionUID = 1L;
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
