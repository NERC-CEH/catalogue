package uk.ac.ceh.gateway.catalogue.model;

/**
 *
 * @author cjohn
 */
public class ExternalResourceFailureException extends RuntimeException {
    public ExternalResourceFailureException(String mess) {
        super(mess);
    }
}
