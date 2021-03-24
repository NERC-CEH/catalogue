package uk.ac.ceh.gateway.catalogue.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CatalogueUserTest {
    @Test
    public void checkUserWithNullUsernameIsPublic() {
        //Given
        CatalogueUser user = new CatalogueUser();
        
        //When
        boolean isPublic = user.isPublic();
        
        //Then
        assertTrue(isPublic);
    }
    
    
    @Test
    public void checkUserWithUsernameIsNotPublic() {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("james@bo.jones");
        
        //When
        boolean isPublic = user.isPublic();
        
        //Then
        assertFalse(isPublic);
    }
}
