package uk.ac.ceh.gateway.catalogue.gemini.elements;

import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Test;
import static org.junit.Assert.*;

public class ThesaurusNameTest {

    @Test
    public void isRenderable() {
        //Given
        ThesaurusName actual = ThesaurusName.builder().build();
        
        //When
        final boolean renderable = actual.isRenderable();
        
        //Then
        assertThat("ThesaurusName should not be renderable", renderable, equalTo(true));
    }
    
}
