package uk.ac.ceh.gateway.catalogue.upload.simple;

import static java.lang.String.format;

public class FileExistsException extends StorageServiceException {

    private static String TEMPLATE = "Could not upload %s, file already exists";

    public FileExistsException(String id, String filename) {
        super(id, format(TEMPLATE, filename));
    }
}
