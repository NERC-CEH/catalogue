package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Builder;
import lombok.Value;

@Value
public class ResourceConstraint {
    private final String value, label, uri;
    
    @Builder
    @JsonCreator
    public ResourceConstraint(
        @JsonProperty("value") String value,
        @JsonProperty("label") String label,
        @JsonProperty("uri") String uri
    ) {
        this.value = nullToEmpty(value);
        this.label = nullToEmpty(label);
        this.uri = nullToEmpty(uri);
    }
}
