package uk.ac.ceh.gateway.catalogue.model;

import lombok.Data;

@Data
public class DocumentUploadFile {
    private final String name;
    private final String path;
    private final String format;
    private final String mediatype;
    private final String encoding;
    private final long bytes;
    private final String hash;
    private String type = "DATA";

    public void setType(String type) {
        this.type = type;
    }

    public void setType(DocumentUpload.Type type) {
        this.type = type.toString();
    }
}
