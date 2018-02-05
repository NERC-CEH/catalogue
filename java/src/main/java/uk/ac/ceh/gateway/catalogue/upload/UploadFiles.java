package uk.ac.ceh.gateway.catalogue.upload;

import java.util.Map;

import com.google.common.collect.Maps;

import lombok.Data;

@Data
public class UploadFiles {
    private final String path;
    private Map<String, UploadFile> documents = Maps.newHashMap();
    private Map<String, UploadFile> invalid = Maps.newHashMap();
    private boolean zipped;
}