package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;
import java.time.LocalDate;
import lombok.Builder;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateSerializer;

@Value
public class TimePeriod {
    @JsonSerialize(using = LocalDateSerializer.class)
    public final LocalDate begin, end;

    @Builder
    @JsonCreator
    private TimePeriod(
        @JsonProperty("begin") String begin,
        @JsonProperty("end") String end) {
        this.begin = LocalDateFactory.parse(begin);
        this.end = LocalDateFactory.parse(end);
    }
}
