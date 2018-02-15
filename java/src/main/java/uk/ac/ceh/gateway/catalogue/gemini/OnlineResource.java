package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import java.util.regex.Pattern;
import lombok.Value;
import lombok.Builder;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.WMS_GET_CAPABILITIES;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.OTHER;

@Value
public class OnlineResource {
    private static final Pattern GET_CAPABILITIES_URL_PATTERN = Pattern.compile("[\\?\\&]request=getcapabilities");
    private static final Pattern WMS_SERVICE_URL_PATTERN = Pattern.compile("[\\?\\&]service=wms");
    private String url, name, description, function;
     
    public enum Type {
        WMS_GET_CAPABILITIES, OTHER
    }
    
    @Builder
    @JsonCreator
    private OnlineResource(
        @JsonProperty("url") String url,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("function") String function) {
        this.url = nullToEmpty(url);
        this.name = nullToEmpty(name);
        this.description = nullToEmpty(description);
        this.function = nullToEmpty(function);
    }
    
    public Type getType() {
        String lowercaseUrl = url.toLowerCase();
        if(GET_CAPABILITIES_URL_PATTERN.matcher(lowercaseUrl).find()) {
            if(WMS_SERVICE_URL_PATTERN.matcher(lowercaseUrl).find()) {
                return WMS_GET_CAPABILITIES;
            }
        }
        return OTHER;
    }
}