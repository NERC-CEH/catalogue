package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;
import java.time.LocalDate;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateDeserializer;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateSerializer;

@Value
public class TimePeriod {
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate begin, end;

    public TimePeriod(String begin, String end) {
        this.begin = LocalDateFactory.parse(begin);
        this.end = LocalDateFactory.parse(end);
    }
}