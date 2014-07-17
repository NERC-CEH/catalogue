package uk.ac.ceh.gateway.catalogue.gemini.elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.Value;

@Value
public class TimePeriod {
    private final LocalDate begin, end;

    public TimePeriod(String begin, String end) {
        this.begin = parseEmptyString(begin);
        this.end = parseEmptyString(end);
    }
    
    private LocalDate parseEmptyString(String date) {
        if (date.isEmpty()) {
            return null;
        } else {
            try {
                return LocalDate.parse(date);
            } catch (DateTimeParseException ex) {
                return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            }
        }
    }
}