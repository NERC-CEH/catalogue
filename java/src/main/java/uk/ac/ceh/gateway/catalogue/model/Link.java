package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import static com.google.common.base.Strings.nullToEmpty;

@Value
public class Link {
    String title, publicationStatus, href, rel, associationType, geometry;

    @Builder
    @JsonCreator
    private Link(
        @JsonProperty("title") String title,
        @JsonProperty("publicationStatus") String publicationStatus,
        @JsonProperty("href") String href,
        @JsonProperty("rel") String rel,
        @JsonProperty("associationType") String associationType,
        @JsonProperty("geometry") String geometry
    ) {
        this.title = nullToEmpty(title);
        this.publicationStatus = nullToEmpty(publicationStatus);
        this.href = nullToEmpty(href);
        this.rel = nullToEmpty(rel);
        this.associationType = nullToEmpty(associationType);
        this.geometry = nullToEmpty(geometry);
    }
}
