package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.Data;

@Data
public class File {
    private String name;
    private String format;
    private String size;
    /** Media type of the format, e.g. text/csv; absent for old agreements and "Other" formats. */
    private String formatType;
    /** Exact size in bytes; wins over the legacy size text when both are present. */
    private Long bytes;
}
