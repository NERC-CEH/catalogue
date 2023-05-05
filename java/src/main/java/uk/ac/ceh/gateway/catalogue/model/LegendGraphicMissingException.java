package uk.ac.ceh.gateway.catalogue.model;

public class LegendGraphicMissingException extends IllegalArgumentException {
    static final long serialVersionUID = 1L;

    public LegendGraphicMissingException(String mess) {
        super(mess);
    }
    
}
