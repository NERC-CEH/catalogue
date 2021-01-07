package uk.ac.ceh.gateway.catalogue.upload.simple;

import static java.lang.String.format;

public class StorageServiceException extends RuntimeException {
    private final String id;

    public StorageServiceException(String id, String message) {
        super(message);
        this.id = id;
    }

    public StorageServiceException(String id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }

    @Override
    public String getMessage() {
        return format("%s for %s", super.getMessage(), id);
    }

}
