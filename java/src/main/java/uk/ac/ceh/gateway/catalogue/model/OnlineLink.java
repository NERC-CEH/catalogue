package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class OnlineLink {
    private final String name, description, url, function;

    @Builder
    @JsonCreator
    private OnlineLink(
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("url") String url,
        @JsonProperty("function") String function
    ) {
        this.name = nullToEmpty(name);
        this.description = nullToEmpty(description);
        this.url = nullToEmpty(url);
        this.function = nullToEmpty(function);
    }
}
