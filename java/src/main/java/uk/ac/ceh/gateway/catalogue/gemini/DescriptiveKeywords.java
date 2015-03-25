package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
public class DescriptiveKeywords {
    private final List<Keyword> keywords;
    private final String type;
    private final ThesaurusName thesaurusName;
    
    @Builder
    @JsonCreator
    private DescriptiveKeywords(@JsonProperty("keywords") List<Keyword> keywords,
        @JsonProperty("type") String type, @JsonProperty("thesaurusName") ThesaurusName thesaurusName) {
        if (keywords == null) {
            this.keywords = Collections.EMPTY_LIST;
        } else {
            this.keywords = keywords;
        }
        this.type = nullToEmpty(type);
        this.thesaurusName = (thesaurusName == null || thesaurusName.isEmpty())? null : thesaurusName;
    }
}