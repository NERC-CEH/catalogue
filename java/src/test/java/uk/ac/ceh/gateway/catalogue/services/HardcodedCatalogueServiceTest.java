package uk.ac.ceh.gateway.catalogue.services;

import static org.hamcrest.Matchers.equalTo;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;

public class HardcodedCatalogueServiceTest {

    @Test
    public void retrieveCatalogueWithKey() {
        //Given
        CatalogueService service = new HardcodedCatalogueService();
        
        //Then
        Catalogue catalogue = service.retrieve("eidc.catalogue.ceh.ac.uk");
        
        //When
        assertThat("Should retrieve EIDC catalogue", catalogue.getTitle(), equalTo("Environmental Information Data Centre"));
    }
    
    @Test(expected = RuntimeException.class)
    public void getDefaultIfNoMatchingKey() {
        //Given
        CatalogueService service = new HardcodedCatalogueService();
        
        //Then
        Catalogue catalogue = service.retrieve("abc\u2606");
        
        //When
        // Throws runtime exception
    }
    
}
