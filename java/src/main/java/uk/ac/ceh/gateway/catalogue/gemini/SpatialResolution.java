package uk.ac.ceh.gateway.catalogue.gemini;

import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.experimental.Builder;

@Value
public class SpatialResolution {
    private final String distance, uom, equivalentScale;

    @Builder
    private SpatialResolution(String distance, String uom, String equivalentScale) {
        this.distance = nullToEmpty(distance);
        this.uom = nullToEmpty(uom);
        this.equivalentScale = nullToEmpty(equivalentScale);
    }   
}