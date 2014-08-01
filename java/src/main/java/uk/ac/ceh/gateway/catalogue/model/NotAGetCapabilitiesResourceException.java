package uk.ac.ceh.gateway.catalogue.model;

/**
 *
 * @author cjohn
 */
public class NotAGetCapabilitiesResourceException extends IllegalArgumentException {
    public NotAGetCapabilitiesResourceException(String mess) {
        super(mess);
    }
}
