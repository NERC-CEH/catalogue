package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.ef.Link;

@Value
public class Geometry {
    private final String geometryString;

    @Builder
    @JsonCreator
    private Geometry(@JsonProperty("geometry") String value) {
        this.geometryString = nullToEmpty(value);
    }
}
