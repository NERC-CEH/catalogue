package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.Builder;

@Value
public class SpatialResolution {
    private final String distance, uom, equivalentScale;

    @Builder
    @JsonCreator
    private SpatialResolution(
        @JsonProperty("distance") String distance, 
        @JsonProperty("uom") String uom,
        @JsonProperty("equivalentScale") String equivalentScale) {
        this.distance = nullToEmpty(distance);
        this.uom = nullToEmpty(uom);
        this.equivalentScale = nullToEmpty(equivalentScale);
    }   
}
