package uk.ac.ceh.gateway.catalogue.gemini.elements;

import lombok.Value;
import lombok.experimental.Builder;

@Value
@Builder
public class CodeListItem {
    
    private String codeList, value;
    
}
