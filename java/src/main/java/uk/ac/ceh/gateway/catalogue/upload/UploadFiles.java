package uk.ac.ceh.gateway.catalogue.upload;

import java.util.Map;
import lombok.Data;

@Data
public class UploadFiles {
    private final String path;
    private final Map<String, UploadFile> documents;
    private final Map<String, UploadFile> invalid;
    private final boolean zipped;
}