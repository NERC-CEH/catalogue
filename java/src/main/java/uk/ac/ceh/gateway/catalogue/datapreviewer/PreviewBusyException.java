package uk.ac.ceh.gateway.catalogue.datapreviewer;

/**
 * Every document-conversion worker is busy, so the document was not converted.
 *
 * <p>This says nothing about the document itself. Callers must not cache it as the document's
 * result, or a busy moment would hide a good document for the life of the cache entry. The preview
 * endpoint maps it to 503 Service Unavailable so the browser can try again later.
 */
public class PreviewBusyException extends RuntimeException {

    public PreviewBusyException() {
        super("All document conversion workers are busy");
    }
}
