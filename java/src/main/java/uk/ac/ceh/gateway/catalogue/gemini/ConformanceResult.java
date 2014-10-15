package uk.ac.ceh.gateway.catalogue.gemini;

import static com.google.common.base.Strings.nullToEmpty;
import java.time.LocalDate;
import lombok.Value;
import lombok.experimental.Builder;

@Value
public class ConformanceResult {
    private final String title, dateType, explanation;
    private final LocalDate date;
    private final boolean pass;

    @Builder
    private ConformanceResult(String title, String dateType, String explanation, LocalDate date, boolean pass) {
        this.title = nullToEmpty(title);
        this.dateType = nullToEmpty(dateType);
        this.explanation = nullToEmpty(explanation);
        this.date = date;
        this.pass = pass;
    }    
}