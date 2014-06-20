package uk.ac.ceh.gateway.catalogue.gemini.elements;

import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Test;
import static org.junit.Assert.*;

public class ThesaurusNameTest {

    @Test
    public void isEmpty() {
        //Given
        ThesaurusName actual = ThesaurusName.builder().build();
        
        //When
        final boolean empty = actual.hasRenderableContent();
        
        //Then
        assertThat("ThesaurusName should be empty", empty, equalTo(true));
    }
    
}
