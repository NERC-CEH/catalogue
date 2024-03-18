package uk.ac.ceh.gateway.catalogue.model;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource.IdentityPermissions;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class MetadataInfoTest {
    @Test
    public void checkThatMetadataInfoCanParseMediaType() {
        //Given
        MediaType type = MediaType.IMAGE_JPEG;
        MetadataInfo info = MetadataInfo.builder().rawType(type.toString()).build();

        //When
        MediaType infoType = info.getRawMediaType();

        //Then
        assertEquals(type, infoType);
    }

    @Test
    public void canHideMediaTypeFromMetadataInfo() {
        //Given
        MetadataInfo info = MetadataInfo.builder().rawType("application/xml").build();

        //When
        info = info.withRawType(null);

        //Then
        assertNull(info.getRawType());
    }

    @Test
    public void cannotAddEditPermissionToPublicGroup() {
        //Given
        MetadataInfo info = MetadataInfo.builder().build();

        //When
        info.addPermission(Permission.EDIT, "public");

        //Then
        assertThat("Public should not have edit permission", info.getIdentities(Permission.EDIT), equalTo(Collections.EMPTY_LIST));
    }

    @Test
    public void canAddEditPermissionToNonPublicGroup() {
        //Given
        MetadataInfo info = MetadataInfo.builder().build();

        //When
        info.addPermission(Permission.EDIT, "ceh");

        //Then
        assertThat("CEH should have edit permission", info.getIdentities(Permission.EDIT), hasItem("ceh"));
    }

    @Test
    public void metadataIsNotPubliclyViewable() {
        //Given
        MetadataInfo info = MetadataInfo.builder().build();

        //When
        boolean actual = info.isPubliclyViewable(Permission.VIEW);

        //Then
        assertThat("Public should not be able to view", actual, equalTo(false));
    }

    @Test
    public void metadataIsPubliclyViewable() {
        //Given
        MetadataInfo info = MetadataInfo.builder().state("published").build();
        info.addPermission(Permission.VIEW, "public");

        //When
        boolean actual = info.isPubliclyViewable(Permission.VIEW);

        //Then
        assertThat("Public should be able to view", actual, equalTo(true));
    }

    @Test
    public void metadataUserCanAccess() {
        //Given
        MetadataInfo info = MetadataInfo.builder().build();
        ;
        info.addPermission(Permission.VIEW, "ceh");

        //When
        boolean actual = info.canAccess(Permission.VIEW, new CatalogueUser("test", "ceh"), createGroups("ceh", "nerc"));

        //Then
        assertThat("Public should be able to view", actual, equalTo(true));
    }

    @Test
    public void replacePermissions() {
        //Given
        MetadataInfo original = MetadataInfo.builder().build();
        ;
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
    public void failToRemoveAllPermissions() {
        //Given
        MetadataInfo original = MetadataInfo.builder().build();
        ;
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
    public void failToRemoveIfOnlyPublicLeft() {
        //Given
        MetadataInfo original = MetadataInfo.builder().build();
        original.addPermission(Permission.VIEW, "test1");
        Set<IdentityPermissions> permissions = new HashSet<>();
        //Only public identift
        permissions.add(IdentityPermissions.builder().identity("public").canView(true).build());

        //When
        MetadataInfo updated = original.replaceAllPermissions(permissions);

        //Then
        assertThat("test1 should still be able to view", updated.getIdentities(Permission.VIEW), hasItem("test1"));
//        assertThat("public should not be able to view", updated.getIdentities(Permission.VIEW), not(hasItem("public")));
    }

    @Test
    public void userInReadonlyGroupCanView() {
        //Given
        MetadataInfo info = MetadataInfo.builder().build();
        CatalogueUser readonly = new CatalogueUser("readonly", "readonly@ceh.ac.uk");

        //When
        boolean actual = info.canAccess(Permission.VIEW, readonly, createGroups("ROLE_CIG_READONLY"));

        //Then
        assertThat("Readonly user should be able to view", actual, is(true));
    }

    @Test
    public void userInReadonlyGroupCannotEdit() {
        //Given
        MetadataInfo info = MetadataInfo.builder().build();
        CatalogueUser readonly = new CatalogueUser("readonly", "readonly@ceh.ac.uk");

        //When
        boolean actual = info.canAccess(Permission.EDIT, readonly, createGroups("ROLE_CIG_READONLY"));

        //Then
        assertThat("Readonly user should not be able to edit", actual, is(false));
    }

    @Test
    public void userInReadonlyGroupCannotDelete() {
        //Given
        MetadataInfo info = MetadataInfo.builder().build();
        CatalogueUser readonly = new CatalogueUser("readonly", "readonly");

        //When
        boolean actual = info.canAccess(Permission.DELETE, readonly, createGroups("ROLE_CIG_READONLY"));

        //Then
        assertThat("Readonly user should not be able to delete", actual, is(false));
    }

    @Test
    public void userInPublisherGroupCanView() {
        //Given
        MetadataInfo info = MetadataInfo.builder().catalogue("eidc").build();
        CatalogueUser publisher = new CatalogueUser("publisher", "publisher");

        //When
        boolean actual = info.canAccess(Permission.VIEW, publisher, createGroups("ROLE_EIDC_PUBLISHER"));

        //Then
        assertThat("Publisher should be able to view", actual, is(true));
    }

    @Test
    public void canCheckIfDocumentIsPublicIfStateIsNull() {
        //Given
        MetadataInfo info = MetadataInfo.builder().build();
        info.addPermission(Permission.VIEW, "public");

        //When
        boolean isPublic = info.isPubliclyViewable(Permission.VIEW);

        //Then
        assertThat("Should not be public unless the state is published", isPublic, is(false));
    }

    private List<Group> createGroups(String... groupNames) {
        List<Group> toReturn = new ArrayList<>();
        for (String groupName : groupNames) {
            toReturn.add(new Group() {

                @Override
                public String getName() {
                    return groupName;
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
