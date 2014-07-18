package uk.ac.ceh.gateway.catalogue.gemini.elements;

import lombok.Value;
import lombok.experimental.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class CodeListItem {
    private final String codeList, value;

    @Builder
    private CodeListItem(String codeList, String value) {
        this.codeList = nullToEmpty(codeList);
        this.value = nullToEmpty(value);
    }   
}