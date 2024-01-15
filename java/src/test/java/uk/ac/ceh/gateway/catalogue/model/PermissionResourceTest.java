package uk.ac.ceh.gateway.catalogue.model;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.PermissionResource.IdentityPermissions;

public class PermissionResourceTest {

    @Test
    public void userWithFullPermissionsAndPublicView() {
        //Given
        MetadataInfo info = MetadataInfo.builder().build();
        info.addPermission(Permission.VIEW, "public");
        info.addPermission(Permission.VIEW, "test");
        info.addPermission(Permission.EDIT, "test");
        info.addPermission(Permission.DELETE, "test");
        MetadataDocument document = new GeminiDocument()
            .setTitle("test")
            .setUri("http://example.com/documents/123")
            .setMetadata(info);
        PermissionResource resource = new PermissionResource(document);
        Set<IdentityPermissions> expected = new HashSet<>();
        expected.add(IdentityPermissions.builder().identity("public").canView(true).build());
        expected.add(IdentityPermissions.builder().identity("test").canView(true).canEdit(true).canDelete(true).build());
        
        //When
        Set<IdentityPermissions> actual = resource.getPermissions();
        
        //Then
        assertThat("Actual permissions should equal expected", actual, equalTo(expected));
    }   
}
