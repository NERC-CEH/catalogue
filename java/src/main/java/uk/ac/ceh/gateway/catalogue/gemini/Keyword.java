package uk.ac.ceh.gateway.catalogue.gemini;

import lombok.Value;
import lombok.experimental.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class Keyword {
    private final String value, URI;
    
    @Builder
    private Keyword(String value, String URI) {
        this.value = nullToEmpty(value);
        this.URI = nullToEmpty(URI);
    }
}