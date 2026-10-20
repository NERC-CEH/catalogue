package uk.ac.ceh.gateway.catalogue.datapreviewer;

/**
 * The record's data is not openly accessible, so nothing about its files may be shown.
 *
 * <p>Deliberately not Spring Security's {@code AccessDeniedException}: the security filter chain
 * turns that into a login redirect for anonymous visitors, where the preview endpoint wants a 403.
 */
public class PreviewNotOpenException extends RuntimeException {

    public PreviewNotOpenException(String recordId) {
        super("Data for record " + recordId + " is not openly accessible");
    }
}
