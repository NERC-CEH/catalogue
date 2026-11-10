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
        return isNotFound(this);
    }

    /**
     * Whether an exception, or anything that caused it, is the datastore reporting a missing file.
     * Readers wrap that report in their own exceptions ({@code DocumentRepositoryException},
     * {@code PermissionDeniedException}, ...), so the whole cause chain is searched.
     */
    public static boolean isNotFound(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof GitFileNotFoundException) {
                return true;
            }
        }
        return false;
    }
}
