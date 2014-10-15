package uk.ac.ceh.gateway.catalogue.gemini;

import static com.google.common.base.Strings.nullToEmpty;
import java.util.Collections;
import java.util.List;
import lombok.Value;
import lombok.experimental.Builder;

@Value
public class DescriptiveKeywords {
    private final List<Keyword> keywords;
    private final String type;
    private final ThesaurusName thesaurusName;
    
    @Builder
    private DescriptiveKeywords(List<Keyword> keywords, String type, ThesaurusName thesaurusName) {
        if (keywords == null) {
            this.keywords = Collections.EMPTY_LIST;
        } else {
            this.keywords = keywords;
        }
        this.type = nullToEmpty(type);
        this.thesaurusName = thesaurusName;
    }
}