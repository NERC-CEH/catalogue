package uk.ac.ceh.gateway.catalogue.gemini.elements;

import java.time.LocalDate;
import lombok.Value;

@Value

public class TimePeriod {
    private final LocalDate begin, end;

    public TimePeriod(String begin, String end) {
        this.begin = LocalDateFactory.parse(begin);
        this.end = LocalDateFactory.parse(end);
    }
}