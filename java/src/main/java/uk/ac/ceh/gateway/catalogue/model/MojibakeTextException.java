package uk.ac.ceh.gateway.catalogue.model;

/**
 * Thrown when a document being saved contains text matching the telltale signature of
 * double-encoded ("mojibake") text - UTF-8 bytes that have been decoded as CP1252/Latin-1
 * and then re-encoded as UTF-8 (see dri-one #328). Catching this at save time stops any
 * remaining ingest path from baking further corrupted literals into the store, even one
 * this fix did not anticipate.
 */
public class MojibakeTextException extends RuntimeException {
    static final long serialVersionUID = 1L;
    public MojibakeTextException(String message) {
        super(message);
    }
}
