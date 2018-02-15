package uk.ac.ceh.gateway.catalogue.indexing;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import uk.ac.ceh.gateway.catalogue.util.ClassMap;

public class IndexGeneratorRegistryTest {
    @Test(expected = DocumentIndexingException.class)
    public void checkThatThrowsExceptionIfNoClassIsFound() throws DocumentIndexingException {
        //Given
        ClassMap<IndexGenerator> classMap = mock(ClassMap.class);
        IndexGeneratorRegistry registry = new IndexGeneratorRegistry(classMap);
        when(classMap.get(String.class)).thenReturn(null);
        
        //When
        registry.generateIndex("This would need a string entry");
        
        //Then
        fail("Expected an exception to be thrown");
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
