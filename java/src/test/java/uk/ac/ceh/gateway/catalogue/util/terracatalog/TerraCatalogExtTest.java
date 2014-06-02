package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import java.util.Properties;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author cjohn
 */
public class TerraCatalogExtTest {
    
    @Test
    public void checkThatOwnerCanBeReadFromProperties() {
        //Given
        Properties properties = new Properties();
        properties.setProperty("owner", "myOwner");
        TerraCatalogExt ext = new TerraCatalogExt(properties);
        
        //When
        String owner = ext.getOwner();
        
        //Then
        assertEquals("Expected to read the correct owner", "myOwner", owner);
    }
    
    @Test
    public void checkThatOwnerGroupCanBeReadFromProperties() {
        //Given
        Properties properties = new Properties();
        properties.setProperty("ownerGroup", "myOwnerGroup");
        TerraCatalogExt ext = new TerraCatalogExt(properties);
        
        //When
        String ownerGroup = ext.getOwnerGroup();
        
        //Then
        assertEquals("Expected to read the correct owner group", "myOwnerGroup", ownerGroup);
    }
    
    @Test
    public void checkThatStatusCanBeReadFromProperties() {
        //Given
        Properties properties = new Properties();
        properties.setProperty("status", "myStatus");
        TerraCatalogExt ext = new TerraCatalogExt(properties);
        
        //When
        String status = ext.getStatus();
        
        //Then
        assertEquals("Expected to read the correct status", "myStatus", status);
    }
    
    @Test
    public void checkThatProtectionCanBeReadFromProperties() {
        //Given
        Properties properties = new Properties();
        properties.setProperty("protection", "myProtection");
        TerraCatalogExt ext = new TerraCatalogExt(properties);
        
        //When
        String protection = ext.getProtection();
        
        //Then
        assertEquals("Expected to read the correct protection", "myProtection", protection);
    }
}
