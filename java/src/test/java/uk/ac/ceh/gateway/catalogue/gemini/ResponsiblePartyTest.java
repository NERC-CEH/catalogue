package uk.ac.ceh.gateway.catalogue.gemini;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ResponsiblePartyTest {
    
    @Test
    public void authorIsHumanReadable() {
        //Given
        ResponsibleParty author = ResponsibleParty.builder().role("author").build();
        String expected = "Author";
        
        //When
        String actual = author.getRole();
        
        //Then
        assertThat("actual role should equal expected", actual, equalTo(expected));
    }
    
    @Test
    public void resourceProviderIsHumanReadable() {
        //Given
        ResponsibleParty author = ResponsibleParty.builder().role("resourceProvider").build();
        String expected = "Resource Provider";
        
        //When
        String actual = author.getRole();
        
        //Then
        assertThat("actual role should equal expected", actual, equalTo(expected));
    }

}