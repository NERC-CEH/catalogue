package uk.ac.ceh.gateway.catalogue.upload;

import java.util.Map;

import com.google.common.collect.Maps;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UploadFiles {
    private String path;
    private String physicalLocation;
    private Map<String, UploadFile> documents = Maps.newHashMap();
    private Map<String, UploadFile> invalid = Maps.newHashMap();
    private boolean zipped;
    private UploadDocumentPagination pagination;
}