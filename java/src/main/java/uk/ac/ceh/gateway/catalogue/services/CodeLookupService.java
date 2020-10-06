package uk.ac.ceh.gateway.catalogue.services;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * The following service will lookup a property from a properties file where the
 * key is in the form: [key].[value].
 * 
 * This service is useful for looking up the English textual value for some 
 * internal code
 */
@Slf4j
@ToString
public class CodeLookupService {
    private final Properties properties;

    public CodeLookupService(Properties properties) {
        this.properties = properties;
        log.info("Creating {}", this);
    }

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
    
    /**
     * Looks up a value from the property value where the key is in the 
     * form: [key].[value].[subkey]
     * 
     * So in the call: lookup("my.complexType", "topic", "bubble") will search for the 
     * property "my.complexType.topic.bubble"
     * 
     * @param key The prefix of the property to lookup.
     * @param value An object which will be turned in to text for looking up
     * @param subkey The suffix of the property lookup.
     * @return the property value if present, otherwise null
     */
    public String lookup(String key, Object value, String subkey) {
        return properties.getProperty(new StringBuilder(key)
            .append(".")
            .append(value)
            .append(".")
            .append(subkey)
            .toString());
    }
}
