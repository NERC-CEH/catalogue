package uk.ac.ceh.gateway.catalogue.model;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
