package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import static com.google.common.base.Strings.nullToEmpty;
import java.util.regex.Pattern;
import lombok.Value;
import lombok.experimental.Builder;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.WMS_GET_CAPABILITIES;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.OTHER;

/**
 *
 * @author cjohn
 */
@Value
@JsonIgnoreProperties({"type"})
public class OnlineResource {
    private static final Pattern GET_CAPABILITIES_URL_PATTERN = Pattern.compile("[\\?\\&]request=getcapabilities");
    private static final Pattern WMS_SERVICE_URL_PATTERN = Pattern.compile("[\\?\\&]service=wms");
    private String url, name, description, function;
     
    public enum Type {
        WMS_GET_CAPABILITIES, OTHER
    }
    
    @Builder
    private OnlineResource(String url, String name, String description, String function) {
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
