package uk.ac.ceh.gateway.catalogue.model;

/**
 *
 * @author cjohn
 */
public class IllegalOgcRequestTypeException extends IllegalArgumentException {
    public IllegalOgcRequestTypeException(String mess) {
        super(mess);
    }
}
