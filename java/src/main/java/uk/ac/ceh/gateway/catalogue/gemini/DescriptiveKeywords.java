package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;

@Value
public class DescriptiveKeywords {
    private final List<Keyword> keywords;
    private final String type;
    
    @Builder
    @JsonCreator
    private DescriptiveKeywords(@JsonProperty("keywords") List<Keyword> keywords,
        @JsonProperty("type") String type) {
        if (keywords == null) {
            this.keywords = Collections.emptyList();
        } else {
            this.keywords = keywords;
        }
        this.type = nullToEmpty(type);
    }
}
