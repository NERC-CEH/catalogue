package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.Builder;

@Value
public class Supplemental {
    private final String name, description, url;

    @Builder
    @JsonCreator
    private Supplemental(
        @JsonProperty("name") String name, 
        @JsonProperty("description") String description,
        @JsonProperty("url") String url){
        this.name = nullToEmpty(name);
        this.description = nullToEmpty(description);
        this.url = nullToEmpty(url);
    }   
}
