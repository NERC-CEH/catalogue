package uk.ac.ceh.gateway.catalogue.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource.IdentityPermissions;

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
    
    @Test
    public void replacePermissions() {
        //Given
        MetadataInfo original = new MetadataInfo();
        original.addPermission(Permission.VIEW, "test1");
        Set<IdentityPermissions> permissions = new HashSet<>();
        permissions.add(IdentityPermissions.builder().identity("another").canView(true).build());
        
        //When
        MetadataInfo updated = original.replaceAllPermissions(permissions);
        
        //Then
        assertThat("test1 should not be able to view", updated.getIdentities(Permission.VIEW), not(hasItem("test1")));
        assertThat("another should not be able to view", updated.getIdentities(Permission.VIEW), hasItem("another"));
    }
    
    @Test
    public void failToremoveAllPermissions() {
        //Given
        MetadataInfo original = new MetadataInfo();
        original.addPermission(Permission.VIEW, "test1");
        Set<IdentityPermissions> permissions = new HashSet<>();
        //An identity with no permissions
        permissions.add(IdentityPermissions.builder().identity("another").build());
        
        //When
        MetadataInfo updated = original.replaceAllPermissions(permissions);
        
        //Then
        assertThat("test1 should still be able to view", updated.getIdentities(Permission.VIEW), hasItem("test1"));
    }
    
    @Test
    public void failToremoveIfOnlyPublicLeft() {
        //Given
        MetadataInfo original = new MetadataInfo();
        original.addPermission(Permission.VIEW, "test1");
        Set<IdentityPermissions> permissions = new HashSet<>();
        //Only public identift
        permissions.add(IdentityPermissions.builder().identity("public").canView(true).build());
        
        //When
        MetadataInfo updated = original.replaceAllPermissions(permissions);
        
        //Then
        assertThat("test1 should still be able to view", updated.getIdentities(Permission.VIEW), hasItem("test1"));
        assertThat("public should not be able to view", updated.getIdentities(Permission.VIEW), not(hasItem("public")));
    }
    
    @Test
    public void userInReadonlyGroupCanView() {
        //Given
        MetadataInfo info = new MetadataInfo();
        CatalogueUser readonly = new CatalogueUser().setUsername("readonly");
        
        //When
        boolean actual = info.canAccess(Permission.VIEW, readonly, createGroups("ROLE_CIG_READONLY"));
        
        //Then
        assertThat("Readonly user should be able to view", actual, is(true));
    }
    
    @Test
    public void userInReadonlyGroupCannotEdit() {
        //Given
        MetadataInfo info = new MetadataInfo();
        CatalogueUser readonly = new CatalogueUser().setUsername("readonly");
        
        //When
        boolean actual = info.canAccess(Permission.EDIT, readonly, createGroups("ROLE_CIG_READONLY"));
        
        //Then
        assertThat("Readonly user should not be able to edit", actual, is(false));
    }
    
    @Test
    public void userInReadonlyGroupCannotDelete() {
        //Given
        MetadataInfo info = new MetadataInfo();
        CatalogueUser readonly = new CatalogueUser().setUsername("readonly");
        
        //When
        boolean actual = info.canAccess(Permission.DELETE, readonly, createGroups("ROLE_CIG_READONLY"));
        
        //Then
        assertThat("Readonly user should not be able to delete", actual, is(false));
    }
    
    @Test
    public void userInPublisherGroupCanView() {
        //Given
        MetadataInfo info = new MetadataInfo();
        CatalogueUser publisher = new CatalogueUser().setUsername("publisher");
        
        //When
        boolean actual = info.canAccess(Permission.VIEW, publisher, createGroups("ROLE_CIG_PUBLISHER"));
        
        //Then
        assertThat("Publisher should be able to view", actual, is(true));
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