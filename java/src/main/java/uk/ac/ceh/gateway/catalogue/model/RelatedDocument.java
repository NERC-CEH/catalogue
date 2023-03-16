package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class RelatedDocument {
    private final String title, href, rel, associationType, status;

    @Builder
    @JsonCreator
    private RelatedDocument(
        @JsonProperty("title") String title,
        @JsonProperty("href") String href,
        @JsonProperty("rel") String rel,
        @JsonProperty("associationType") String associationType,
        @JsonProperty("status") String status
    ) {
        this.title = nullToEmpty(title);
        this.href = nullToEmpty(href);
        this.rel = nullToEmpty(rel);
        this.associationType = nullToEmpty(associationType);
        this.status = nullToEmpty(status);
    }
}
