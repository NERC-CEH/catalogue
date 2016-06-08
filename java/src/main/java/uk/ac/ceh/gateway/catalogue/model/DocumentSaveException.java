package uk.ac.ceh.gateway.catalogue.model;

public class DocumentSaveException extends RuntimeException {

    public DocumentSaveException() {
        super();
    }

    public DocumentSaveException(String message) {
        super(message);
    }

    public DocumentSaveException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocumentSaveException(Throwable cause) {
        super(cause);
    }

    public DocumentSaveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}