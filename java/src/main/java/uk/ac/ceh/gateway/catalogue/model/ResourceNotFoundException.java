package uk.ac.ceh.gateway.catalogue.model;

public class ResourceNotFoundException extends IllegalArgumentException {
    static final long serialVersionUID = 1L;
    public ResourceNotFoundException(String mess) {
        super(mess);
    }
}
