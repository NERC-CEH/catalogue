package uk.ac.ceh.gateway.catalogue.indexing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.util.ClassMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IndexGeneratorRegistryTest {
    @Test
    public void checkThatThrowsExceptionIfNoClassIsFound() throws DocumentIndexingException {
        Assertions.assertThrows(DocumentIndexingException.class, () -> {
            //Given
            ClassMap<IndexGenerator> classMap = mock(ClassMap.class);
            IndexGeneratorRegistry registry = new IndexGeneratorRegistry(classMap);
            when(classMap.get(String.class)).thenReturn(null);

            //When
            registry.generateIndex("This would need a string entry");

            //Then
            fail("Expected an exception to be thrown");
        });
    }
    
    @Test
    public void checkThatDelegatesToAFoundIndex() throws DocumentIndexingException {
        //Given
        String expectedIndex = "Expected Index";
        ClassMap<IndexGenerator> classMap = mock(ClassMap.class);
        IndexGenerator delegatee = mock(IndexGenerator.class);
        IndexGeneratorRegistry registry = new IndexGeneratorRegistry(classMap);
        when(classMap.get(String.class)).thenReturn(delegatee);
        when(delegatee.generateIndex("delegate")).thenReturn(expectedIndex);
        
        //When
        Object index = registry.generateIndex("delegate");
        
        //Then
        assertThat(index, is(expectedIndex));
    }
}
