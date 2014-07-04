package uk.ac.ceh.gateway.catalogue.linking;

class DocumentLinkingException extends Exception {

    public DocumentLinkingException() {
    }

    public DocumentLinkingException(String message) {
        super(message);
    }

    public DocumentLinkingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocumentLinkingException(Throwable cause) {
        super(cause);
    }

    public DocumentLinkingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}