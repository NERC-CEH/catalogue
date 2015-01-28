package uk.ac.ceh.gateway.catalogue.services;

import java.util.Properties;
import lombok.Data;

/**
 * The following service will lookup a given 
 * @author cjohn
 */
@Data
public class CodeLookupService {
    private final Properties properties;
    
    public String lookup(String key, Object value) {
        return properties.getProperty(key + "." + String.valueOf(value));
    }
}
