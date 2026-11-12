package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.Data;

/**
 * A set of files the depositor expects to upload, described by a naming pattern rather than listed one by one.
 */
@Data
public class FileGroup {
    private String pattern;
    private Integer count;
    private String format;
    private String size;
    private String description;
}
