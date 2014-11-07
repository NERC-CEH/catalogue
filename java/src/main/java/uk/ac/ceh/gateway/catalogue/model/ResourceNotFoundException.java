package uk.ac.ceh.gateway.catalogue.model;

/**
 *
 * @author cjohn
 */
public class ResourceNotFoundException extends IllegalArgumentException {
    public ResourceNotFoundException(String mess) {
        super(mess);
    }
}
