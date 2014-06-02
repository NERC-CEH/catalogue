package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author cjohn
 */
public class TerraCatalogExtReaderTest {
    private TerraCatalogExtReader reader;
    
    @Before
    public void createTerraCatalogExtReader() {
        reader = new TerraCatalogExtReader();
    }
    
    @Test
    public void checkThatOwnerCanBeReadFromProperties() {
        //Given
        Properties properties = new Properties();
        properties.setProperty("owner", "myOwner");
        TerraCatalogExt ext = reader.readTerraCatalogExt(properties);
        
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
        TerraCatalogExt ext = reader.readTerraCatalogExt(properties);
        
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
        TerraCatalogExt ext = reader.readTerraCatalogExt(properties);
        
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
        TerraCatalogExt ext = reader.readTerraCatalogExt(properties);
        
        //When
        String protection = ext.getProtection();
        
        //Then
        assertEquals("Expected to read the correct protection", "myProtection", protection);
    }
    
    @Test
    public void checkThatCanReadFromTCExtInputStream() throws UnsupportedEncodingException, IOException {
        //Given
        String inputStr = "owner=cj\n" +
                          "ownerGroup=ceh\n" +
                          "status=internal\n" +
                          "protection=protected";
        
        InputStream input = new ByteArrayInputStream(inputStr.getBytes("UTF-8"));
        
        //When
        TerraCatalogExt read = reader.readTerraCatalogExt(input);
        
        //Then
        assertEquals("Expected the owner to by cj", "cj", read.getOwner());
        assertEquals("Expected the ownerGroup to by ceh", "ceh", read.getOwnerGroup());
        assertEquals("Expected the status to by internal", "internal", read.getStatus());
        assertEquals("Expected the protection to by protected", "protected", read.getProtection());
    }
}
