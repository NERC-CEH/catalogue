package uk.ac.ceh.gateway.catalogue.model;

/**
 * This is the interface for a metadata document. Specific implementations such 
 * as #GeminiDocument and #UKEOFDocument must implement this in order to be used
 * by the various components of this catalogue.
 * @author cjohn
 */
public interface MetadataDocument {
    String getDescription();
    String getTitle();
    String getId();
    String getType();
    MetadataInfo getMetadata();
    void attachMetadata(MetadataInfo metadata);
}
