package uk.ac.ceh.gateway.catalogue.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CatalogueUserTest {
    @Test
    public void checkUserWithNullUsernameIsPublic() {
        //Given
        CatalogueUser user = new CatalogueUser();
        
        //When
        boolean isPublic = user.isPublic();
        
        //Then
        assertTrue("No username supplied, expected to be public", isPublic);
    }
    
    
    @Test
    public void checkUserWithUsernameIsNotPublic() {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("james@bo.jones");
        
        //When
        boolean isPublic = user.isPublic();
        
        //Then
        assertFalse("Expected user with username to not be public", isPublic);
    }
}
