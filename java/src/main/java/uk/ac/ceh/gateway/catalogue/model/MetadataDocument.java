package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;

/**
 * This is the interface for a metadata document. Specific implementations such 
 * as #GeminiDocument and #UKEOFDocument must implement this in order to be used
 * by the various components of this catalogue.
 * @author cjohn
 */
public interface MetadataDocument {
    String getUri();
    MetadataDocument setUri(String uri);
    String getDescription();
    MetadataDocument setDescription(String description);
    String getTitle();
    MetadataDocument setTitle(String title);
    String getId();
    MetadataDocument setId(String id);
    String getType();
    MetadataDocument setType(String type);
    MetadataInfo getMetadata();
    MetadataDocument setMetadata(MetadataInfo metadata);
    MetadataDocument setMetadataDate(LocalDateTime date);
    LocalDateTime getMetadataDate();
    List<ResourceIdentifier> getResourceIdentifiers();
    MetadataDocument setResourceIdentifiers(List<ResourceIdentifier> resourceIdentifiers);
    List<Keyword> getAllKeywords();
    MetadataDocument addAdditionalKeywords(List<Keyword> additionalKeywords);
    
    @JsonIgnore
    default String getCatalogue() {
        return Optional.ofNullable(getMetadata())
            .map(MetadataInfo::getCatalogue)
            .orElse("");
    }
    
    @JsonIgnore
    default String getMetadataDateTime() {
        /* This method always returns the full datetime string (including the seconds). LocalDateTime.toString() will 
           not return the seconds if it is a date with time 00:00:00 */
        return Optional.ofNullable(getMetadataDate())
            .map(md -> md.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .orElse("");
    }
}