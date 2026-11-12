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
    /** Media type of the format, e.g. text/csv; absent for old agreements and "Other" formats. */
    private String formatType;
    /** Per-file size range in bytes (equal when exact), and the group total; win over the legacy size text. */
    private Long minBytes;
    private Long maxBytes;
    private Long totalBytes;
    private String description;
}
