package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;

@Value
public class RelatedRecord {
    @NonNull String rel, identifier;
    String href, title, associationType;

    @JsonCreator
    public RelatedRecord(
        @JsonProperty("rel") String rel,
        @JsonProperty("identifier") String identifier,
        @JsonProperty("href") String href,
        @JsonProperty("title") String title,
        @JsonProperty("associationType") String associationType
    ) {
        this.rel = rel;
        this.identifier = identifier;
        this.href = href;
        this.title = title;
        this.associationType = associationType;
    }
}
