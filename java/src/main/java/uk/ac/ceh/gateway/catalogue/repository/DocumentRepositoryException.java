package uk.ac.ceh.gateway.catalogue.repository;

import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;

public class DocumentRepositoryException extends Exception {
    static final long serialVersionUID = 1L;

    public DocumentRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Whether the read failed because the record does not exist, rather than a fault reading it.
     * The repository wraps the datastore's {@link GitFileNotFoundException} rather than returning
     * null, so callers that answer 404 must check this.
     */
    public boolean isNotFound() {
        for (Throwable cause = this; cause != null; cause = cause.getCause()) {
            if (cause instanceof GitFileNotFoundException) {
                return true;
            }
        }
        return false;
    }
}
