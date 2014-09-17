package uk.ac.ceh.gateway.catalogue.gemini;

import lombok.Value;
import java.time.LocalDate;

@Value
public class TimePeriod {
    private final LocalDate begin, end;

    public TimePeriod(String begin, String end) {
        this.begin = LocalDateFactory.parse(begin);
        this.end = LocalDateFactory.parse(end);
    }
}