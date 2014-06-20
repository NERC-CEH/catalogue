package uk.ac.ceh.gateway.catalogue.gemini.elements;

import lombok.Value;
import lombok.experimental.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class CodeListItem implements Renderable {
    private final String codeList, value;
    public static CodeListItem EMPTY_CODE_LIST_ITEM = new CodeListItem("", "");

    @Builder
    private CodeListItem(String codeList, String value) {
        this.codeList = nullToEmpty(codeList);
        this.value = nullToEmpty(value);
    }
    
    @Override
    public boolean isRenderable() {
        return codeList.isEmpty() && value.isEmpty();
    }
    
}
