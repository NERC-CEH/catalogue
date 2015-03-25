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
public class ConformanceResult {
    private final String title, dateType, explanation;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate date;
    private final boolean pass;

    @Builder
    @JsonCreator
    private ConformanceResult(
        @JsonProperty("title") String title, 
        @JsonProperty("dateType") String dateType, 
        @JsonProperty("explanation") String explanation, 
        @JsonProperty("date") LocalDate date, 
        @JsonProperty("pass") boolean pass) {
        this.title = nullToEmpty(title);
        this.dateType = nullToEmpty(dateType);
        this.explanation = nullToEmpty(explanation);
        this.date = date;
        this.pass = pass;
    }    
}