package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This is the interface for a metadata document. Specific implementations such
 * as #GeminiDocument and #UKEOFDocument must implement this in order to be used
 * by the various components of this catalogue.
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
    String getMetadataDateTime();
    List<ResourceIdentifier> getResourceIdentifiers();
    MetadataDocument setResourceIdentifiers(List<ResourceIdentifier> resourceIdentifiers);
    List<Keyword> getAllKeywords();
    MetadataDocument addAdditionalKeywords(List<Keyword> additionalKeywords);
    Set<Relationship> getRelationships();
    MetadataDocument setRelationships(Set<Relationship> relationships);

    @JsonIgnore
    default void validate() {}

    @JsonIgnore
    default String getCatalogue() {
        return Optional.ofNullable(getMetadata())
            .map(MetadataInfo::getCatalogue)
            .orElse("");
    }

    @JsonIgnore
    default String getState() {
        return Optional.ofNullable(getMetadata())
            .map(MetadataInfo::getState)
            .orElse("draft");
    }
}
