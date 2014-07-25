package uk.ac.ceh.gateway.catalogue.gemini.elements;

import lombok.Value;
import java.time.LocalDate;
import uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument.common.DateHandler;

@Value
public class TimePeriod {
    private final LocalDate begin, end;

    public TimePeriod(String begin, String end) {
        this.begin = DateHandler.parseEmptyString(begin);
        this.end = DateHandler.parseEmptyString(end);
    }
}