package uk.ac.ceh.gateway.catalogue.gemini.elements;

import java.util.List;
import lombok.Value;
import lombok.experimental.Builder;

@Value
@Builder
public class DescriptiveKeywords {
    
    private List<String> keywords;
    private CodeListItem type;
    
}
