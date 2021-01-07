package uk.ac.ceh.gateway.catalogue.upload.simple;

import static java.lang.String.format;

public class FileExitsException extends StorageServiceException {

    private static String TEMPLATE = "Could not upload %s, file already exists";

    public FileExitsException(String id, String filename) {
        super(id, format(TEMPLATE, filename));
    }
}
