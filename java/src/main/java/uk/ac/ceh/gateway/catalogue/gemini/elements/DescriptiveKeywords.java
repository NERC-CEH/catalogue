package uk.ac.ceh.gateway.catalogue.gemini.elements;

import java.util.List;
import lombok.Value;
import lombok.experimental.Builder;

@Value
@Builder
public class DescriptiveKeywords {
    
    private List<Keyword> keywords;
    private CodeListItem type;
    private ThesaurusName thesaurusName;
}
