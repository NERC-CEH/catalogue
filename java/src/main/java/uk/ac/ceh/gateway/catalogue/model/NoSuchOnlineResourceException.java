package uk.ac.ceh.gateway.catalogue.model;

/**
 *
 * @author cjohn
 */
public class NoSuchOnlineResourceException extends IllegalArgumentException {
    public NoSuchOnlineResourceException(String mess) {
        super(mess);
    }
}
