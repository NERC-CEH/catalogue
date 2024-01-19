package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.Builder;

@Value
public class SpatialReferenceSystem {
    private final String code, codeSpace, title;

    @Builder
    @JsonCreator
    private SpatialReferenceSystem(
        @JsonProperty("code") String code,
        @JsonProperty("codeSpace") String codeSpace,
        @JsonProperty("title") String title){
        this.code = nullToEmpty(code);
        this.codeSpace = nullToEmpty(codeSpace);
        this.title = nullToEmpty(title);
    }
}
