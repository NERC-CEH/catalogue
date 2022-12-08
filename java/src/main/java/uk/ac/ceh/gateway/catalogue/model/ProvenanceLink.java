package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;

@Value
public class ProvenanceLink {
    @NonNull String rel, identifierFrom, identifierTo;
    String description, hrefFrom, hrefTo, titleFrom, titleTo, associationType;

    @JsonCreator
    public ProvenanceLink(
        @JsonProperty("identifierFrom") String identifierFrom,
        @JsonProperty("hrefFrom") String hrefFrom,
        @JsonProperty("titleFrom") String titleFrom,
        @JsonProperty("rel") String rel,
        @JsonProperty("identifierTo") String identifierTo,
        @JsonProperty("hrefTo") String hrefTo,
        @JsonProperty("titleTo") String titleTo,
        @JsonProperty("description") String description,
        @JsonProperty("associationType") String associationType
    ) {
        this.identifierFrom = identifierFrom;
        this.hrefFrom = hrefFrom;
        this.titleFrom = titleFrom;
        this.rel = rel;
        this.identifierTo = identifierTo;
        this.hrefTo = hrefTo;
        this.titleTo = titleTo;
        this.description = description;
        this.associationType = associationType;
    }
}
