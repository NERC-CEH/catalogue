package uk.ac.ceh.gateway.catalogue.gemini.elements;

import lombok.Value;

/**
 *
 * @author cjohn
 */
@Value
public class OnlineResource {
    private String url, name, description;
    
     public OnlineResource(String url, String name, String description) {
        this.url = url;
        this.name = name;
        this.description = description;
    }
}
