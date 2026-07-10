package uk.ac.ceh.gateway.catalogue.catalogue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Value
public class CatalogueViewResource {
    String id;
    List<String> value;

    public CatalogueViewResource(MetadataDocument document) {
        this.id = document.getId();
        this.value = document.getMetadata().getCatalogueView();
    }

    @JsonCreator
    public CatalogueViewResource(
        @JsonProperty("id") String id,
        @JsonProperty("value") List<String> value
    ) {
        this.id = id;
        this.value = Optional.ofNullable(value).orElse(Collections.emptyList());
    }
}
