package uk.ac.ceh.gateway.catalogue.ef;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
public class Geometry {
    private final String wkt;
    
    @Builder
    @JsonCreator
    private Geometry(
        @JsonProperty("westBoundLongitude") String wkt) {
        this.wkt = wkt;
    }
}
