package uk.ac.ceh.gateway.catalogue.gemini.elements;

import java.util.Collections;
import java.util.List;
import lombok.Value;
import lombok.experimental.Builder;

@Value
public class DescriptiveKeywords {
    private final List<Keyword> keywords;
    private final CodeListItem type;
    private final ThesaurusName thesaurusName;
    
    @Builder
    private DescriptiveKeywords(List<Keyword> keywords, CodeListItem type, ThesaurusName thesaurusName) {
        if (keywords == null) {
            this.keywords = Collections.EMPTY_LIST;
        } else {
            this.keywords = keywords;
        }
        this.type = type;
        this.thesaurusName = thesaurusName;
    }
}