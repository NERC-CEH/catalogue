package uk.ac.ceh.gateway.catalogue.upload;

import lombok.Data;

@Data
public class UploadDocumentPagination {
    private int page;
    private int size;
    private int total;
}