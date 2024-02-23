package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.NonNull;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.model.Relationship;

import java.util.Optional;

@Value
// TODO: To be removed once data migrated to Relationships
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

    public Optional<Relationship> toRelationship() {
        if (Strings.isNullOrEmpty(rel) || Strings.isNullOrEmpty(href)) {
            return Optional.empty();
        } else {
            return Optional.of(new Relationship(rel, href));
        }
    }
}
