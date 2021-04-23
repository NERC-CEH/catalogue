package uk.ac.ceh.gateway.catalogue.services;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CodeLookupServiceTest {
    private final CodeLookupService service = new CodeLookupService("codelist.properties");
    
    @Test
    void checkThatTryingToGetValueForMissingContentReturnsNull() {
        //Given
        //Nothing
        
        //When
        val actual = service.lookup("something.which.isnt", "here");
        
        //Then
        assertNull(actual);
    }
    
    @Test
    void checkThatCanLookupNormalValue() {
        //Given
        val expected = "Attribute";
        
        //When
        val actual = service.lookup("metadata.resourceType", "attribute");
        
        //Then
        assertEquals(expected, actual);
    }
    
    @Test
    void checkThatCanLookupValueWithSubKey() {
        //Given
        val expected = "Attribute";
        
        //When
        val actual = service.lookup("metadata", "resourceType", "attribute");
        
        //Then
        assertEquals(expected, actual);
    }
}
