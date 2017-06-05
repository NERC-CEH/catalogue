package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;

@Value
public class CatalogueResource {
    private final String id, value;

    @JsonCreator
    public CatalogueResource(
        @JsonProperty("id") @NonNull String id,
        @JsonProperty("value") @NonNull String value
    ) {
        this.id = id;
        this.value = value;
    }       
}
