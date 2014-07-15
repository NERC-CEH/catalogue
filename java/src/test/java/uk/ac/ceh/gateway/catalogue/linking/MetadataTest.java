package uk.ac.ceh.gateway.catalogue.linking;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.elements.ResourceIdentifier;

public class MetadataTest {
    
    @Test
    public void canGetInternalResourceIdentifier() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setResourceIdentifiers(new HashSet<>(Arrays.asList(
            ResourceIdentifier.builder().codeSpace("CEH:EIDC:").code("1204954356374").build()
        )));
        String expected = "CEH:EIDC:#1204954356374";
        
        //When
        String actual = new Metadata(document).getResourceIdentifier();
        
        //Then
        assertThat("actual resourceIdentifer should equal expected", actual, equalTo(expected));
    } 
    
    @Test
    public void doesNotHaveIdentifier() {
        //Given
        GeminiDocument document = new GeminiDocument();
        String expected = "";
        
        //When
        String actual = new Metadata(document).getResourceIdentifier();
        
        //Then
        assertThat("actual resourceIdentifer should equal expected", actual, equalTo(expected));
    } 
}