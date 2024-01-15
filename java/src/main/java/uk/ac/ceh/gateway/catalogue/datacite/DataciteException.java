package uk.ac.ceh.gateway.catalogue.datacite;

public class DataciteException extends RuntimeException {
    static final long serialVersionUID = 1L;

    public DataciteException() {
        super();
    }

    public DataciteException(String mess) {
        super(mess);
    }

    public DataciteException(Throwable cause) {
        super(cause);
    }

    public DataciteException(String mess, Throwable cause) {
        super(mess, cause);
    }
}
