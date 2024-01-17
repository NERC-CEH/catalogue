package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import java.time.LocalDate;
import lombok.Value;
import lombok.Builder;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateDeserializer;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateSerializer;

@Value
public class InspireTheme {
    private final String theme, uri, conformity;

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
