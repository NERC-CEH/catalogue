package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.Builder;

@Value
public class AccessLimitation {
    private final String value, code, description, uri;

    @Builder
    @JsonCreator
    public AccessLimitation(
        @JsonProperty("value") String value,
        @JsonProperty("code") String code,
        @JsonProperty("description") String description,
        @JsonProperty("uri") String uri){
        this.value = nullToEmpty(value);
        this.code = nullToEmpty(code);
        this.description = nullToEmpty(description);
        this.uri = nullToEmpty(uri);
    }

}
