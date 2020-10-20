package uk.ac.ceh.gateway.catalogue.permission;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.crowd.model.CrowdGroup;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.model.PermissionDeniedException;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class CrowdPermissionServiceTest {
    @Mock
    private DataRepository<CatalogueUser> repo;
    @Mock
    private DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    @Mock
    private GroupStore<CatalogueUser> groupStore;
    @InjectMocks
    private CrowdPermissionService permissionService;

    private MetadataInfo publik() {
        val publicMetadataInfo = MetadataInfo.builder().state("published").build();
        publicMetadataInfo.addPermission(Permission.VIEW, "public");
        return publicMetadataInfo;
    }

    private MetadataInfo draft() {
        return MetadataInfo.builder().state("DRAFT").build();
    }

    private CatalogueUser populateSecurityContextHolder(String username, String... roleNames) {
        val user = new CatalogueUser().setUsername(username);
        List<Group> roles = Arrays.stream(roleNames)
                .map(CrowdGroup::new)
                .collect(Collectors.toList());
        given(groupStore.getGroups(user)).willReturn(roles);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);
        return user;
    }

    @SneakyThrows
    private void configDocumentInfoMapper(MetadataInfo info) {
        given(repo.getLatestRevision()).willReturn(new DummyRevision("revision"));
        given(repo.getData("revision", "test.meta")).willAnswer(RETURNS_MOCKS);
        given(documentInfoMapper.readInfo(any(InputStream.class))).willReturn(info);
    }

    @Test
    public void namedUserWithUploadPermissionCanUpload() {
        //given
        populateSecurityContextHolder("uploader");
        val info = MetadataInfo.builder().build();
        info.addPermission(Permission.UPLOAD, "uploader");
        configDocumentInfoMapper(info);

        //when
        val actual = permissionService.userCanUpload("test");

        //then
        assertTrue(actual);
    }

    @Test
    public void namedUserWithEditPermissionCanNotUpload() {
        //given
        populateSecurityContextHolder("edit");
        val info = MetadataInfo.builder().build();
        info.addPermission(Permission.EDIT, "edit");
        configDocumentInfoMapper(info);

        //when
        val actual = permissionService.userCanUpload("test");

        //then
        assertFalse(actual);
    }

    @Test
    @SneakyThrows
    public void adminCanUpload() {
        //given
        populateSecurityContextHolder("admin", "ROLE_CIG_SYSTEM_ADMIN");
        configDocumentInfoMapper(MetadataInfo.builder().build());

        //when
        val actual = permissionService.userCanUpload("test");

        //then
        assertTrue(actual);
        verify(repo, never()).getLatestRevision();
    }

    @Test
    public void publisherCanEditRestricted() {
        //Given
        populateSecurityContextHolder("publisher", "ROLE_EIDC_PUBLISHER");

        //When
        val actual = permissionService.userCanEditRestrictedFields("eidc");

        //Then
        assertTrue(actual);
    }

    @Test
    public void editorCanEditRestricted() {
        //Given
        populateSecurityContextHolder("editor", "ROLE_EIDC_EDITOR");

        //When
        val actual = permissionService.userCanEditRestrictedFields("eidc");

        //Then
        assertTrue(actual);
    }

    @Test
    public void namedEditorCannotEditRestricted() {
        //Given
        populateSecurityContextHolder("editor");
        configDocumentInfoMapper(MetadataInfo.builder().catalogue("eidc").build());

        //When
        val actual = permissionService.userCanEditRestrictedFields("eidc");

        //Then
        assertFalse(actual);
    }

    @Test
    public void eidcEditorCanCreate() {
        //given
        populateSecurityContextHolder("eidcEditor", "ROLE_EIDC_EDITOR");
        
        //when
        val actual = permissionService.userCanCreate("eidc");
        
        //then
        assertTrue(actual);
    }

    @SneakyThrows
    @Test(expected = PermissionDeniedException.class)
    public void anonymousCanNotAccessUnknownRecord() {
        //Given
        given(repo.getData("revision", "test.meta")).willThrow(DataRepositoryException.class);
        
        //When
        permissionService.toAccess(CatalogueUser.PUBLIC_USER, "test", "revision", "VIEW");
        
        //Then
        fail("Should not be able to get metadata record for unknown");
    }

    @SneakyThrows
    @Test
    public void anonymousCanAccessPublicRecord() {
        //Given
        configDocumentInfoMapper(publik());
        
        //When
        val actual = permissionService.toAccess(CatalogueUser.PUBLIC_USER, "test", "revision", "VIEW");
        
        //Then
        assertTrue(actual);
    }

    @SneakyThrows
    @Test
    public void namedUserCanAccessPublicRecord() {
        //Given
        val namedUser = populateSecurityContextHolder("named");
        configDocumentInfoMapper(publik());
        
        //When
        val actual = permissionService.toAccess(namedUser, "test", "revision", "VIEW");
        
        //Then
        assertTrue(actual);
    }

    @SneakyThrows
    @Test
    public void anonymousCanNotAccessDraftRecord() {
        //Given
        configDocumentInfoMapper(draft());
        
        //When
        val actual = permissionService.toAccess(CatalogueUser.PUBLIC_USER, "test", "revision", "VIEW");
        
        //Then
        assertFalse(actual);
    }
    
    @Test
    @SneakyThrows
    public void namedUserCanAccessDraftRecordWithUsernamePermission() {
        //Given
        val namedUser = populateSecurityContextHolder("username");

        val metadataInfo = draft();
        metadataInfo.addPermission(Permission.VIEW, "username");
        configDocumentInfoMapper(metadataInfo);
        
        //When
        val actual = permissionService.toAccess(namedUser, "test", "revision", "VIEW");
        
        //Then
        assertTrue(actual);
    }
    
    @Test
    public void namedUserCanViewDraftRecordWithGroupPermission() {
        //Given
        val namedUser = populateSecurityContextHolder("username","group0");
        val metadataInfo = draft();
        metadataInfo.addPermission(Permission.VIEW, "group0");
        configDocumentInfoMapper(metadataInfo);
        given(groupStore.getGroups(namedUser)).willReturn(Collections.singletonList(new CrowdGroup("group0")));
        
        //When
        val actual = permissionService.toAccess(namedUser, "test", "revision", "VIEW");
        
        //Then
        assertTrue(actual);
    }
    
    @Test
    @SneakyThrows
    public void namedUserCannotWriteDraftRecordWithNoGroupPermission() {
        //Given
        val namedUser = populateSecurityContextHolder("username");
        configDocumentInfoMapper(draft());
        given(groupStore.getGroups(namedUser)).willReturn(Collections.emptyList());
        
        //When
        val actual = permissionService.toAccess(namedUser, "test", "revision", "EDIT");
        
        //Then
        assertFalse(actual);
    }
    
    @Test
    public void editorCanEdit() {
        //Given
        populateSecurityContextHolder("editor");
        MetadataInfo info = MetadataInfo.builder().catalogue("eidc").build();
        info.addPermission(Permission.EDIT, "editor");
        configDocumentInfoMapper(info);
        
        //When
        val actual = permissionService.userCanEdit("test");
        
        //Then
        assertTrue(actual);
    }
    
    @Test
    public void publisherCanMakePublic() {
        //Given
        populateSecurityContextHolder("publisher", "ROLE_EIDC_PUBLISHER");
        
        //When
        val actual = permissionService.userCanMakePublic("eidc");
        
        //Then
        assertTrue(actual);
    }
    
    @Test
    public void publisherCanView() {
        //Given
        CatalogueUser publisher = populateSecurityContextHolder("publisher","ROLE_EIDC_PUBLISHER");
        configDocumentInfoMapper(MetadataInfo.builder().catalogue("eidc").build());

        //When
        val actual = permissionService.toAccess(publisher, "test", "VIEW");
        
        //Then
        assertTrue(actual);
    }
    
    @Test
    public void nonPublisherCannotMakePublic() {
        //Given
        populateSecurityContextHolder("editor","ROLE_EIDC_EDITOR");
        
        //When
        val actual = permissionService.userCanMakePublic("eidc");
        
        //Then
        assertFalse(actual);
    }
    
    @Test
    public void userWithoutEditorPermissionCannotEdit() {
        //Given
        populateSecurityContextHolder("bob","CEH", "Another");
        configDocumentInfoMapper(MetadataInfo.builder().catalogue("ceh").build());
        
        //When
        val actual = permissionService.userCanEdit("test");
        
        //Then
        assertFalse(actual);
    }
    
    @Test
    public void publicCannotEdit() {
        //Given
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(CatalogueUser.PUBLIC_USER);
        SecurityContextHolder.setContext(securityContext);

        configDocumentInfoMapper(MetadataInfo.builder().catalogue("ceh").build());
        
        //When
        val actual = permissionService.userCanEdit("test");
        
        //Then
        assertFalse(actual);
    }

    @Value
    private static class DummyRevision implements DataRevision<CatalogueUser> {
        String revision;

        @Override
        public String getRevisionID() {
            return revision;
        }

        @Override
        public String getMessage() {
            return null;
        }

        @Override
        public String getShortMessage() {
            return null;
        }

        @Override
        public CatalogueUser getAuthor() {
            return null;
        }
    }

}