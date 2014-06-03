package uk.ac.ceh.gateway.catalogue.gemini.elements;

import lombok.Value;
import lombok.experimental.Builder;

@Value
@Builder
public class CodeListValue {
    
    private String codeList, value;
    
}
