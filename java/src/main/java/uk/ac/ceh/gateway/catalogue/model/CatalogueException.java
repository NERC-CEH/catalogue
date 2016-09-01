package uk.ac.ceh.gateway.catalogue.model;

public class CatalogueException extends RuntimeException {

    public CatalogueException(String message) {
        super(message);
    }

    public CatalogueException(String message, Throwable cause) {
        super(message, cause);
    }

    public CatalogueException(Throwable cause) {
        super(cause);
    }

}
