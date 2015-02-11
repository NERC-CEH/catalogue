package uk.ac.ceh.gateway.catalogue.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.userstore.Group;

/**
 *
 * @author cjohn
 */
public class MetadataInfoTest {
    @Test
    public void checkThatMetadataInfoCanParseMediaType() {
        //Given
        MediaType type = MediaType.IMAGE_JPEG;
        MetadataInfo info = new MetadataInfo();
        info.setRawType(type.toString());
        
        //When
        MediaType infoType = info.getRawMediaType();
        
        //Then
        assertEquals("Expected the mediatypes to be equal", type, infoType);
    }
    
    @Test
    public void canHideMediaTypeFromMetadataInfo() {
        //Given
        MetadataInfo info = new MetadataInfo();
        info.setRawType("application/xml");
        
        //When
        info.hideMediaType();
        
        //Then
        assertNull("Expected no media type to be specified", info.getRawType());
    }
 
    @Test
    public void cannotAddEditPermissionToPublicGroup() {
        //Given
        MetadataInfo info = new MetadataInfo();
        
        //When
        info.addPermission(Permission.EDIT, "public");
        
        //Then
        assertThat("Public should not have edit permission", info.getIdentities(Permission.EDIT), equalTo(Collections.EMPTY_LIST));
    }
    
    @Test
    public void canAddEditPermissionToNonPublicGroup() {
        //Given
        MetadataInfo info = new MetadataInfo();
        
        //When
        info.addPermission(Permission.EDIT, "ceh");
        
        //Then
        assertThat("CEH should have edit permission", info.getIdentities(Permission.EDIT), hasItem("ceh"));
    }
    
    @Test
    public void metadataIsNotPubliclyViewable() {
        //Given
        MetadataInfo info = new MetadataInfo();
        
        //When
        boolean actual = info.isPubliclyViewable(Permission.VIEW);
        
        //Then
        assertThat("Public should not be able to view", actual, equalTo(false));
    }
    
    @Test
    public void metadataIsPubliclyViewable() {
        //Given
        MetadataInfo info = new MetadataInfo().setState("published");
        info.addPermission(Permission.VIEW, "public");
        
        //When
        boolean actual = info.isPubliclyViewable(Permission.VIEW);
        
        //Then
        assertThat("Public should be able to view", actual, equalTo(true));
    }
    
    @Test
    public void metadataUserCanAccess() {
        //Given
        MetadataInfo info = new MetadataInfo();
        info.addPermission(Permission.VIEW, "ceh");
        
        //When
        boolean actual = info.canAccess(Permission.VIEW, new CatalogueUser().setUsername("test"), createGroups("ceh", "nerc"));
        
        //Then
        assertThat("Public should be able to view", actual, equalTo(true));
    }
    
    private List<Group> createGroups(String... groupnames) {
        List<Group> toReturn = new ArrayList<>();
        for (String groupname : groupnames) {
            toReturn.add(new Group() {

                @Override
                public String getName() {
                    return groupname;
                }

                @Override
                public String getDescription() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
        }
        return toReturn;
    }
}