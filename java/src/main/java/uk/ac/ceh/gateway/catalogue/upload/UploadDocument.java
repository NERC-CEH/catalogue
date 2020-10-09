package uk.ac.ceh.gateway.catalogue.upload;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class UploadDocument {
    private String id;
    private Map<String, UploadFiles> uploadFiles = new HashMap<>();
}