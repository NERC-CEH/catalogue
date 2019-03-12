package uk.ac.ceh.gateway.catalogue.upload;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class UploadDocument {
    private String id;
    private Map<String, UploadFiles> uploadFiles = new HashMap<String, UploadFiles>();
}