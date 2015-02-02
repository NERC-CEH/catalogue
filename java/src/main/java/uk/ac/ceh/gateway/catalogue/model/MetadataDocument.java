package uk.ac.ceh.gateway.catalogue.model;

import java.net.URI;
import java.util.List;

/**
 * This is the interface for a metadata document. Specific implementations such 
 * as #GeminiDocument and #UKEOFDocument must implement this in order to be used
 * by the various components of this catalogue.
 * @author cjohn
 */
public interface MetadataDocument {
    URI getUri();
    String getDescription();
    String getTitle();
    String getId();
    String getType();
    List<String> getLocations();
    List<String> getTopics();
    MetadataInfo getMetadata();
    void attachMetadata(MetadataInfo metadata);
    void attachUri(URI uri);
}