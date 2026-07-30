package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.OTHER;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.WMS_GET_CAPABILITIES;

@Value
public class OnlineResource {
    private static final Pattern GET_CAPABILITIES_URL_PATTERN = Pattern.compile("[?&]request=getcapabilities", CASE_INSENSITIVE);
    private static final Pattern WMS_SERVICE_URL_PATTERN = Pattern.compile("[?&]service=wms", CASE_INSENSITIVE);
    private static final List<String> EIDC_DISTRIBUTION_PREFIXES = List.of(
        "https://order-eidc.ceh.ac.uk/resources",
        "https://data-package.ceh.ac.uk/sd/",
        "https://data-package.ceh.ac.uk/data/",
        "https://catalogue.ceh.ac.uk/datastore/eidchub/"
        );

    String url, name, description, function, size;

    public enum Type {
        WMS_GET_CAPABILITIES, OTHER
    }

    @Builder
    @JsonCreator
    private OnlineResource(
        @JsonProperty("url") String url,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("function") String function,
        @JsonProperty("size") String size) {
        this.url = nullToEmpty(url);
        this.name = nullToEmpty(name);
        this.description = nullToEmpty(description);
        this.function = nullToEmpty(function);
        this.size = nullToEmpty(size);
    }

    public Type getType() {
        if(GET_CAPABILITIES_URL_PATTERN.matcher(url).find() &&
            WMS_SERVICE_URL_PATTERN.matcher(url).find()) {
                return WMS_GET_CAPABILITIES;
        }
        return OTHER;
    }

    public boolean isEidcDistribution() {
        return EIDC_DISTRIBUTION_PREFIXES.stream()
        .anyMatch(url::startsWith);
    }
}
