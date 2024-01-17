package uk.ac.ceh.gateway.catalogue.gemini;

public class XPaths {
    public static final String ID = "/*/gmd:fileIdentifier/*";
    public static final String TITLE = "/*/gmd:identificationInfo/*/gmd:citation/*/gmd:title/*";
    public static final String DESCRIPTION = "/*/gmd:identificationInfo/*/gmd:abstract/*";
    public static final String ALTERNATE_TITLE = "/*/gmd:identificationInfo/*/gmd:citation/*/gmd:alternateTitle/*";
    public static final String RESOURCE_TYPE = "/*/gmd:hierarchyLevel/*/@codeListValue";
    public static final String BROWSE_GRAPHIC_URL = "/*/gmd:identificationInfo/*/gmd:graphicOverview/*/gmd:fileName/*";
    public static final String RESOURCE_STATUS = "/*/gmd:identificationInfo/*/gmd:status/*/@codeListValue";
    public static final String METADATA_DATE = "/*/gmd:dateStamp/*";
    public static final String LINEAGE = "/*/gmd:dataQualityInfo/*/gmd:lineage/*/gmd:statement/*";
    public static final String METADATA_STANDARD = "/*/gmd:metadataStandardName/*";
    public static final String METADATA_VERSION = "/*/gmd:metadataStandardVersion/*";
    public static final String SUPPLEMENTAL_INFO = "/*/gmd:identificationInfo/*/gmd:supplementalInformation/*";
    public static final String SPATIAL_REPRESENTATION_TYPE = "/*/gmd:identificationInfo/*/gmd:spatialRepresentationType/*/@codeListValue";
    public static final String DATASET_LANGUAGE = "/*/gmd:identificationInfo/*/gmd:language/*/@codeListValue";
    public static final String USE_CONSTRAINT = "/*/gmd:identificationInfo/*/gmd:resourceConstraints/gmd:MD_Constraints[gmd:useLimitation]";
    public static final String ACCESS_CONSTRAINT = "/*/gmd:identificationInfo/*/gmd:resourceConstraints/gmd:MD_LegalConstraints[gmd:accessConstraints]";
    public static final String SECURITY_CONSTRAINT = "/*/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:classification/*/@codeListValue";
    public static final String RESPONSIBLE_PARTY = "/*/gmd:identificationInfo/*/gmd:pointOfContact/*";
    public static final String DISTRIBUTOR = "/*/gmd:distributionInfo/*/gmd:distributor/*/gmd:distributorContact/*";
    public static final String METADATA_POINT_OF_CONTACT = "/*/gmd:contact/*";
}
