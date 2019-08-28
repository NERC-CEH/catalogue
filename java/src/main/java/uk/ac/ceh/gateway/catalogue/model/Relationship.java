package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;

@Value
public class Relationship {
    private final @NonNull String relation, target;

    @JsonCreator
    public Relationship(
        @JsonProperty("relation") String relation,
        @JsonProperty("target") String target) {
        this.relation = relation;
        this.target = target;
    }
}
