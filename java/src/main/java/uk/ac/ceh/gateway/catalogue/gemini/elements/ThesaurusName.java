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
    private static final LocalDate NULL_DATE = LocalDate.parse("-1000000-01-01");
    public static final ThesaurusName EMPTY_THESAURUS_NAME = new ThesaurusName("", NULL_DATE, CodeListItem.EMPTY_CODE_LIST_ITEM);
    
    @Builder
    private ThesaurusName(String title, LocalDate date, CodeListItem dateType) {
        this.title = nullToEmpty(title);
        
        if (date == null) {
            this.date = NULL_DATE;
        } else {
            this.date = date;
        }
        
        if (dateType == null) {
            this.dateType = CodeListItem.EMPTY_CODE_LIST_ITEM;
        } else {
            this.dateType = dateType;
        }
    }
}