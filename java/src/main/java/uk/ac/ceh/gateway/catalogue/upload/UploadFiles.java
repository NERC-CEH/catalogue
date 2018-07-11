package uk.ac.ceh.gateway.catalogue.upload;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class UploadFiles {
    private String path;
    private String physicalLocation;
    private Map<String, UploadFile> documents = Maps.newHashMap();
    private Map<String, UploadFile> invalid = Maps.newHashMap();
    private boolean zipped;
}