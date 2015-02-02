package uk.ac.ceh.gateway.catalogue.services;

import java.util.Properties;
import lombok.Data;

/**
 * The following service will lookup a property from a properties file where the
 * key is in the form: [key].[value].
 * 
 * This service is useful for looking up the English textual value for some 
 * internal code
 * @author cjohn
 */
@Data
public class CodeLookupService {
    private final Properties properties;
    
    /**
     * Looks up a value from the property value where the key is in the 
     * form: [key].[value]
     * 
     * So in the call: lookup("my.complexType", true) will search for the 
     * property "my.complexType.true"
     * 
     * @param key The prefix of the property to lookup.
     * @param value An object which will be turned in to text for looking up
     * @return the property value if present, otherwise null
     */
    public String lookup(String key, Object value) {
        return properties.getProperty(key + "." + String.valueOf(value));
    }
}
