/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ceh.gateway.catalogue.indexing;


import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;

public class PrioritisedClassMapTest {
    @Test
    public void canFindExactMatch() {
        //Given
        String expectedValue = "Expected value";
        PrioritisedClassMap<String> classMap = new PrioritisedClassMap<>();
        classMap.register(String.class, expectedValue);

        //When
        String value = classMap.get(String.class);

        //Then
        assertThat(value, is(expectedValue));
    }

    @Test
    public void returnsNullIfNoMapping() {
        //Given
        PrioritisedClassMap<String> classMap = new PrioritisedClassMap<>();
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
        PrioritisedClassMap<String> classMap = new PrioritisedClassMap<>();
        classMap.register(Number.class, expectedValue);

        //When
        String value = classMap.get(Integer.class);

        //Then
        assertThat(value, is(expectedValue));
    }

    @Test
    public void returnsFirstRegisteredMatch() {
        //Given
        String expectedValue = "Expected value";
        PrioritisedClassMap<String> classMap = new PrioritisedClassMap<>();
        classMap.register(Number.class, expectedValue);
        classMap.register(Integer.class, "The wrong value");

        //When
        String value = classMap.get(Integer.class);

        //Then
        assertThat(value, is(expectedValue));
    }
}
