package uk.ac.ceh.gateway.catalogue.model;

public class CatalogueNotFoundException extends RuntimeException {

    public CatalogueNotFoundException() {
    }

    public CatalogueNotFoundException(String message) {
        super(message);
    }

    public CatalogueNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CatalogueNotFoundException(Throwable cause) {
        super(cause);
    }

    public CatalogueNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
