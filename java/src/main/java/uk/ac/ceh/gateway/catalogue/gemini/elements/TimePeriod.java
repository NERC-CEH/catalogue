package uk.ac.ceh.gateway.catalogue.gemini.elements;

import java.time.LocalDate;
import lombok.Value;

@Value

public class TimePeriod {
    private static LocalDateFactory FACTORY = new LocalDateFactory();
    private final LocalDate begin, end;

    public TimePeriod(String begin, String end) {
        this.begin = FACTORY.parse(begin);
        this.end = FACTORY.parse(end);
    }
}