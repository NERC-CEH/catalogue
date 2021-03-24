package uk.ac.ceh.gateway.catalogue.services;

import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CodeLookupServiceTest {
    private Properties properties;
    private CodeLookupService service;
    
    @BeforeEach
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
        assertNull(desc);
    }
    
    @Test
    public void checkThatCanLookupBooleanValues() {
        //Given
        String text = "This is so true";
        properties.setProperty("something.true", text);
        
        //When
        String desc = service.lookup("something", true);
        
        //Then
        assertEquals(desc, text);
    }
    
    @Test
    public void checkThatCanLookupNormalValue() {
        //Given
        String text = "This is so normal";
        properties.setProperty("something.just.normal", text);
        
        //When
        String desc = service.lookup("something.just", "normal");
        
        //Then
        assertEquals(desc, text);
    }
    
    @Test
    public void checkThatCanLookupValueWithSubkey() {
        //Given
        String text = "This is so normal";
        properties.setProperty("something.just.normal", text);
        
        //When
        String desc = service.lookup("something", "just", "normal");
        
        //Then
        assertEquals(desc, text);
    }
}
