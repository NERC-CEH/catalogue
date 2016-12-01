package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class Link {
    private final String title, href, associationType;

    @Builder
    @JsonCreator
    private Link(
        @JsonProperty("title") String title,
        @JsonProperty("href") String href,
        @JsonProperty("associationType") String associationType
    ) {
        this.title = nullToEmpty(title);
        this.href = nullToEmpty(href);
        this.associationType = nullToEmpty(associationType);
    }
}