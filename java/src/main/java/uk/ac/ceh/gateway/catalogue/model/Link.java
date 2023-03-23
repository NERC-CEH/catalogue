package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import static com.google.common.base.Strings.nullToEmpty;

@Value
public class Link {
    String title, href, rel, associationType;

    @Builder
    @JsonCreator
    private Link(
        @JsonProperty("title") String title,
        @JsonProperty("href") String href,
        @JsonProperty("rel") String rel,
        @JsonProperty("associationType") String associationType
    ) {
        this.title = nullToEmpty(title);
        this.href = nullToEmpty(href);
        this.rel = nullToEmpty(rel);
        this.associationType = nullToEmpty(associationType);
    }
}
