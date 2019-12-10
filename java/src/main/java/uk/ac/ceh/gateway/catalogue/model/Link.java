package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class Link {
    private final String title, href, associationType, rel;

    @Builder
    @JsonCreator
    private Link(
        @JsonProperty("title") String title,
        @JsonProperty("href") String href,
        @JsonProperty("associationType") String associationType,
        @JsonProperty("rel") String rel
    ) {
        this.title = nullToEmpty(title);
        this.href = nullToEmpty(href);
        this.rel = nullToEmpty(rel);
        this.associationType = nullToEmpty(associationType);
    }
}