package uk.ac.ceh.gateway.catalogue.upload;

import lombok.Data;

@Data
public class UploadFile {
    private String name;
    private String path;
    private String physicalLocation;
    private String id;    
    private String format;
    private String mediatype;
    private String encoding;
    private long bytes;
    private String hash;
    private UploadType type;
}