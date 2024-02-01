package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Builder;
import lombok.Value;

@Value
public class ResourceConstraint {
    private final String value, code, uri;

    @Builder
    @JsonCreator
    public ResourceConstraint(
        @JsonProperty("value") String value,
        @JsonProperty("code") String code,
        @JsonProperty("uri") String uri
    ) {
        this.value = nullToEmpty(value);
        this.code = nullToEmpty(code);
        this.uri = nullToEmpty(uri);
    }
}
