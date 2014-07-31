package uk.ac.ceh.gateway.catalogue.gemini.elements;

import java.util.regex.Pattern;
import lombok.Value;
import static uk.ac.ceh.gateway.catalogue.gemini.elements.OnlineResource.Type.GET_CAPABILITIES;
import static uk.ac.ceh.gateway.catalogue.gemini.elements.OnlineResource.Type.OTHER;

/**
 *
 * @author cjohn
 */
@Value
public class OnlineResource {
    private static final Pattern GET_CAPABILITIES_URL_PATTERN = Pattern.compile("[\\?\\&]request=getcapabilities");
    private String url, name, description;
     
    public enum Type {
        GET_CAPABILITIES, OTHER
    }
    
    public OnlineResource(String url, String name, String description) {
        this.url = url;
        this.name = name;
        this.description = description;
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
