package uk.ac.ceh.gateway.catalogue.gemini;

import lombok.Value;
import lombok.experimental.Builder;
import java.time.LocalDate;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class ThesaurusName {
    private final String title, dateType;
    private final LocalDate date;
    
    @Builder
    private ThesaurusName(String title, LocalDate date, String dateType) {
        this.title = nullToEmpty(title);
        this.date = date;
        this.dateType = nullToEmpty(dateType);
    }
}