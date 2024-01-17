package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.ArrayList;
import java.util.List;

public class DocumentIndexingException extends Exception {
    static final long serialVersionUID = 1L;

    private final List<String> suppressedDocuments = new ArrayList<>();

    public DocumentIndexingException() {
        super();
    }

    public DocumentIndexingException(String mess) {
        super(mess);
    }

    public DocumentIndexingException(Throwable cause) {
        super(cause);
    }

    public DocumentIndexingException(String mess, Throwable cause) {
        super(mess, cause);
    }

    public void addSuppressed(String document, Exception exception) {
        super.addSuppressed(exception);
        suppressedDocuments.add(document);
    }

    public List<String> getSupressedDocuments() {
        return suppressedDocuments;
    }
}
