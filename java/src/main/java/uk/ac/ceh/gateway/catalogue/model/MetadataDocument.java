package uk.ac.ceh.gateway.catalogue.model;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;

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
    List<String> getPartOfRepository();
    void attachPartOfRepository(List<String> repositories);
    MetadataInfo getMetadata();
    void attachMetadata(MetadataInfo metadata);
    void attachUri(URI uri);
    MetadataDocument setId(String id);
    MetadataDocument setMetadataDate(LocalDateTime date);
    List<ResourceIdentifier> getResourceIdentifiers();
    MetadataDocument setResourceIdentifiers(List<ResourceIdentifier> resourceIdentifiers);
}