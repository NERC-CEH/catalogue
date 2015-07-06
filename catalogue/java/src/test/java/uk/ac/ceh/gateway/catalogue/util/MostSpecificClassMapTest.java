/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ceh.gateway.catalogue.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author cjohn
 */
public class MostSpecificClassMapTest {
    @Test
    public void canFindExactMatch() {
        //Given
        String expectedValue = "Expected value";
        MostSpecificClassMap<String> classMap = new MostSpecificClassMap<>();
        classMap.register(String.class, expectedValue);
        
        //When
        String value = classMap.get(String.class);
        
        //Then
        assertThat(value, is(expectedValue));
    }
    
    @Test
    public void returnsNullIfNoMapping() {
        //Given
        MostSpecificClassMap<String> classMap = new MostSpecificClassMap<>();
        classMap.register(Number.class, "not a string");
        
        //When
        String value = classMap.get(String.class);
        
        //Then
        assertThat(value, nullValue());
    }
    
    @Test
    public void canTraverseTheObjectHierarchy() {
        //Given
        String expectedValue = "Expected value";
        MostSpecificClassMap<String> classMap = new MostSpecificClassMap<>();
        classMap.register(Number.class, expectedValue);
        
        //When
        String value = classMap.get(Integer.class);
        
        //Then
        assertThat(value, is(expectedValue));
    }
}
