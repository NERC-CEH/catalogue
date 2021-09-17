package uk.ac.ceh.gateway.catalogue.catalogue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

@Value
public class CatalogueResource {
    String id, value;

    public CatalogueResource(MetadataDocument document) {
        this.id = document.getId();
        this.value = document.getCatalogue();
    }

    @JsonCreator
    public CatalogueResource(
        @JsonProperty("id") @NonNull String id,
        @JsonProperty("value") @NonNull String value
    ) {
        this.id = id;
        this.value = value;
    }
}
