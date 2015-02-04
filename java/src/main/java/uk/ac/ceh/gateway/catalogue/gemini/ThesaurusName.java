package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;
import lombok.experimental.Builder;
import java.time.LocalDate;
import static com.google.common.base.Strings.nullToEmpty;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateDeserializer;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateSerializer;

@Value
@JsonIgnoreProperties("empty")
public class ThesaurusName {
    private final String title, dateType;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate date;
    
    @Builder
    private ThesaurusName(String title, LocalDate date, String dateType) {
        this.title = nullToEmpty(title);
        this.date = date;
        this.dateType = nullToEmpty(dateType);
    }
    
    public boolean isEmpty() {
        return title.isEmpty() && dateType.isEmpty() && date == null;
    }
}