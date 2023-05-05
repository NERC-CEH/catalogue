package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.OTHER;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.WMS_GET_CAPABILITIES;

@Value
public class OnlineResource {
    private static final Pattern GET_CAPABILITIES_URL_PATTERN = Pattern.compile("[?&]request=getcapabilities", CASE_INSENSITIVE);
    private static final Pattern WMS_SERVICE_URL_PATTERN = Pattern.compile("[?&]service=wms", CASE_INSENSITIVE);
    String url, name, description, function;

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
        if(GET_CAPABILITIES_URL_PATTERN.matcher(url).find() &&
            WMS_SERVICE_URL_PATTERN.matcher(url).find()) {
                return WMS_GET_CAPABILITIES;
        }
        return OTHER;
    }
}
