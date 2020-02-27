package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import lombok.Value;
import lombok.Builder;

@Value
public class Supplemental {
    private final String name, description, url, noraID, type, function;

    @Builder
    @JsonCreator
    private Supplemental(
        @JsonProperty("name") String name, 
        @JsonProperty("description") String description,
        @JsonProperty("url") String url,
        @JsonProperty("noraID") String noraID,
        @JsonProperty("type") String type,
        @JsonProperty("function") String function){
        this.name = nullToEmpty(name);
        this.description = nullToEmpty(description);
        this.url = nullToEmpty(url);
        this.noraID = nullToEmpty(noraID);
        this.type = nullToEmpty(type);
        this.function = nullToEmpty(function);
    }   
}
