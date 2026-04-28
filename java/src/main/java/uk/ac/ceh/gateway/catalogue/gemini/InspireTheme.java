package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;

import lombok.Value;
import lombok.Builder;

@Value
public class InspireTheme {
    String theme, uri, conformity;

    @Builder
    @JsonCreator
    private InspireTheme(
        @JsonProperty("theme") String theme,
        @JsonProperty("uri") String uri,
        @JsonProperty("conformity") String conformity) {
        this.theme = nullToEmpty(theme);
        this.uri = nullToEmpty(uri);
        this.conformity = nullToEmpty(conformity);
        }
}
