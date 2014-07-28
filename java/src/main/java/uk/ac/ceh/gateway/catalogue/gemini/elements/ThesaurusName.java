package uk.ac.ceh.gateway.catalogue.gemini.elements;

import lombok.Value;
import lombok.experimental.Builder;
import java.time.LocalDate;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class ThesaurusName {
    private final String title;
    private final LocalDate date;
    private final CodeListItem dateType;
    
    @Builder
    private ThesaurusName(String title, LocalDate date, CodeListItem dateType) {
        this.title = nullToEmpty(title);
        this.date = date;
        this.dateType = dateType;
    }
}