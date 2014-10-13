package uk.ac.ceh.gateway.catalogue.gemini;

import static com.google.common.base.Strings.nullToEmpty;
import java.util.regex.Pattern;
import lombok.Value;
import lombok.experimental.Builder;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.GET_CAPABILITIES;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.OTHER;

/**
 *
 * @author cjohn
 */
@Value
public class OnlineResource {
    private static final Pattern GET_CAPABILITIES_URL_PATTERN = Pattern.compile("[\\?\\&]request=getcapabilities");
    private String url, name, description, function;
     
    public enum Type {
        GET_CAPABILITIES, OTHER
    }
    
    @Builder
    private OnlineResource(String url, String name, String description, String function) {
        this.url = nullToEmpty(url);
        this.name = nullToEmpty(name);
        this.description = nullToEmpty(description);
        this.function = nullToEmpty(function);
    }
    
    public Type getType() {
        if(GET_CAPABILITIES_URL_PATTERN.matcher(url.toLowerCase()).find()) {
            return GET_CAPABILITIES;
        }
        else {
            return OTHER;
        }
    }
}
