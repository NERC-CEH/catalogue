package uk.ac.ceh.gateway.catalogue.services;

import java.util.Properties;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author cjohn
 */
public class CodeLookupServiceTest {
    private Properties properties;
    private CodeLookupService service;
    
    @Before
    public void init() {
        properties = new Properties();
        service = new CodeLookupService(properties);
    }
    
    @Test
    public void checkThatTryingToGetValueForMissingContentReturnsNull() {
        //Given
        //Nothing
        
        //When
        String desc = service.lookup("something.which.isnt", "here");
        
        //Then
        assertNull("Excepected an empty value", desc);
    }
    
    @Test
    public void checkThatCanLookupBooleanValues() {
        //Given
        String text = "This is so true";
        properties.setProperty("something.true", text);
        
        //When
        String desc = service.lookup("something", true);
        
        //Then
        assertEquals("Expected to get the text out", desc, text);
    }
    
    @Test
    public void checkThatCanLookupNormalValue() {
        //Given
        String text = "This is so normal";
        properties.setProperty("something.just.normal", text);
        
        //When
        String desc = service.lookup("something.just", "normal");
        
        //Then
        assertEquals("Expected to get the text out", desc, text);
    }
}
