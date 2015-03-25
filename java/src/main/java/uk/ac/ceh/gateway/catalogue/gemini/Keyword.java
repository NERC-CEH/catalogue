package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;

@Value
public class Keyword {
    private final String value, uri;
       
    @Builder
    @JsonCreator
    private Keyword(@JsonProperty("value") String value, @JsonProperty("uri") String URI) {
        this.value = nullToEmpty(value);
        this.uri = nullToEmpty(URI);
    }
}