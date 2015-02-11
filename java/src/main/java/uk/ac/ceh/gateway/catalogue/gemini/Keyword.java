package uk.ac.ceh.gateway.catalogue.gemini;

import lombok.Value;
import lombok.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class Keyword {
    private final String value, uri;
    
    @Builder
    private Keyword(String value, String URI) {
        this.value = nullToEmpty(value);
        this.uri = nullToEmpty(URI);
    }
}