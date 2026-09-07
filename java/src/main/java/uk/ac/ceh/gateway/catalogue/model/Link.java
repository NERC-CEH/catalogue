package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;

@Value
public class Link {
    String title, publicationStatus, availability, href, rel, associationType, geometry;
    List<ResourceIdentifier> resourceIdentifiers;

    @Builder
    @JsonCreator
    private Link(
        @JsonProperty("title") String title,
        @JsonProperty("publicationStatus") String publicationStatus,
        @JsonProperty("availability") String availability,
        @JsonProperty("href") String href,
        @JsonProperty("rel") String rel,
        @JsonProperty("associationType") String associationType,
        @JsonProperty("geometry") String geometry,
        @JsonProperty("resourceIdentifiers") List<ResourceIdentifier> resourceIdentifiers
    ) {
        this.title = nullToEmpty(title);
        this.publicationStatus = nullToEmpty(publicationStatus);
        this.availability = nullToEmpty(availability);
        this.href = nullToEmpty(href);
        this.rel = nullToEmpty(rel);
        this.associationType = nullToEmpty(associationType);
        this.geometry = nullToEmpty(geometry);
        this.resourceIdentifiers = resourceIdentifiers;
    }

    public List<ResourceIdentifier> getResourceIdentifiers() {
        return Optional.ofNullable(resourceIdentifiers)
            .orElseGet(Collections::emptyList);
    }
}
