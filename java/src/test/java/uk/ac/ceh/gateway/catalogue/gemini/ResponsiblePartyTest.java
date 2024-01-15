package uk.ac.ceh.gateway.catalogue.gemini;

import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;


public class ResponsiblePartyTest {
    
    @Test
    public void authorIsHumanReadable() {
        //Given
        ResponsibleParty author = ResponsibleParty.builder().role("author").build();
        String expected = "Author";
        
        //When
        String actual = author.getRoleDisplayName();
        
        //Then
        assertThat("actual role should equal expected", actual, equalTo(expected));
    }
    
    @Test
    public void resourceProviderIsHumanReadable() {
        //Given
        ResponsibleParty author = ResponsibleParty.builder().role("resourceProvider").build();
        String expected = "Resource Provider";
        
        //When
        String actual = author.getRoleDisplayName();
        
        //Then
        assertThat("actual role should equal expected", actual, equalTo(expected));
    }

}
