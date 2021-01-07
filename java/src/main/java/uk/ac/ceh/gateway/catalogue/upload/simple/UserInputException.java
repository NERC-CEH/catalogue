package uk.ac.ceh.gateway.catalogue.upload.simple;

public class UserInputException extends StorageServiceException {

    public UserInputException(String id, String message) {
        super(id, message);
    }

    public UserInputException(String id, String message, Throwable cause) {
        super(id, message, cause);
    }
}
