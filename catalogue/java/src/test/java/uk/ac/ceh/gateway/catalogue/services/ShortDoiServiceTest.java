package uk.ac.ceh.gateway.catalogue.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.springframework.web.client.RestClientException;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;

/**
 *
 * @author cjohn
 */
public class ShortDoiServiceTest {
    @Test
    public void canObtainAShortDoi() {
        //Given
        ShortDoiService service = new ShortDoiService();
        String doi = "10.5285/cfdb346f-6cde-4fba-9044-96e202398435";
        
        //When
        ResourceIdentifier shortDoi = service.shortenDoi(doi);
        
        //Then
        assertEquals("Expected to get the short doi", shortDoi.getCode(), "10/766");
    }
    
    @Test(expected=RestClientException.class)
    public void failToGetShortDoiForNonDoi() {
        //Given
        ShortDoiService service = new ShortDoiService();
        String doi = "IamNotADOI";
        
        //When
        ResourceIdentifier shortDoi = service.shortenDoi(doi);
        
        //Then
        fail("expected to fail");
    }
}
