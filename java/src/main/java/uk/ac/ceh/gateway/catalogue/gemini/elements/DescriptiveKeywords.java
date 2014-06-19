package uk.ac.ceh.gateway.catalogue.gemini.elements;

import java.util.Collections;
import java.util.List;
import lombok.Value;
import lombok.experimental.Builder;

@Value
public class DescriptiveKeywords implements Renderable {
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
        
        if (type == null) {
            this.type = CodeListItem.EMPTY_CODE_LIST_ITEM;
        } else {
            this.type = type;
        }
        
        if (thesaurusName == null) {
            this.thesaurusName = ThesaurusName.EMPTY_THESAURUS_NAME; 
        } else {
            this.thesaurusName = thesaurusName;
        }
    }

    @Override
    public boolean hasRenderableContent() {
        return keywords.isEmpty() && type.hasRenderableContent() && thesaurusName.hasRenderableContent();
    }
}
