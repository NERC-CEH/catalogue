package uk.ac.ceh.gateway.catalogue.permission;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
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

    private CatalogueUser populateSecurityContextHolder(String username) {
        val user = new CatalogueUser(username, username);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        return user;
    }

    private CatalogueUser populateSecurityContextHolderAndAuthentication(String username, String... roleNames) {
        val user = new CatalogueUser(username, username);
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

    private CatalogueUser publisherCanViewPopulateSecurityContextHolder(String username, String... roleNames) {
        val user = new CatalogueUser(username, username);
        List<Group> roles = Arrays.stream(roleNames)
                .map(CrowdGroup::new)
                .collect(Collectors.toList());
        given(groupStore.getGroups(user)).willReturn(roles);
        SecurityContext securityContext = mock(SecurityContext.class);
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
    void userIsAdmin() {
        //given
        populateSecurityContextHolderAndAuthentication(
            "admin",
            "ROLE_CIG_SYSTEM_ADMIN"
        );

        //when
        assertTrue(permissionService.userIsAdmin());
    }

    @Test
    void userIsNotAdmin() {
        //given
        populateSecurityContextHolderAndAuthentication("not", "ROLE_USER");

        //when
        assertFalse(permissionService.userIsAdmin());
    }

    @Test
    void namedUserWithUploadPermissionCanUpload() {
        //given
        populateSecurityContextHolderAndAuthentication("uploader");
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
        populateSecurityContextHolderAndAuthentication("edit");
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
        populateSecurityContextHolderAndAuthentication("admin", "ROLE_CIG_SYSTEM_ADMIN");
        //configDocumentInfoMapper(MetadataInfo.builder().build());

        //when
        val actual = permissionService.userCanUpload("test");

        //then
        assertTrue(actual);
        verify(repo, never()).getLatestRevision();
    }

    @Test
    public void publisherCanEditRestricted() {
        //Given
        populateSecurityContextHolderAndAuthentication("publisher", "ROLE_EIDC_PUBLISHER");

        //When
        val actual = permissionService.userCanEditRestrictedFields("eidc");

        //Then
        assertTrue(actual);
    }

    @Test
    public void editorCanEditRestricted() {
        //Given
        populateSecurityContextHolderAndAuthentication("editor", "ROLE_EIDC_EDITOR");

        //When
        val actual = permissionService.userCanEditRestrictedFields("eidc");

        //Then
        assertTrue(actual);
    }

    @Test
    public void namedEditorCannotEditRestricted() {
        //Given
        populateSecurityContextHolderAndAuthentication("editor");
        //configDocumentInfoMapper(MetadataInfo.builder().catalogue("eidc").build());

        //When
        val actual = permissionService.userCanEditRestrictedFields("eidc");

        //Then
        assertFalse(actual);
    }

    @Test
    public void eidcEditorCanCreate() {
        //given
        populateSecurityContextHolderAndAuthentication("eidcEditor", "ROLE_EIDC_EDITOR");

        //when
        val actual = permissionService.userCanCreate("eidc");

        //then
        assertTrue(actual);
    }

    @SneakyThrows
    @Test
    public void anonymousCanNotAccessUnknownRecord() {
        Assertions.assertThrows(PermissionDeniedException.class, () -> {
            //Given
            given(repo.getData("revision", "test.meta")).willThrow(DataRepositoryException.class);

            //When
            permissionService.toAccess(CatalogueUser.PUBLIC_USER, "test", "revision", "VIEW");

            //Then
            fail("Should not be able to get metadata record for unknown");
        });
    }

    @SneakyThrows
    @Test
    public void anonymousCanAccessPublicRecord() {
        //Given
        //given(repo.getLatestRevision()).willReturn(new DummyRevision("revision"));
        given(repo.getData("revision", "test.meta")).willAnswer(RETURNS_MOCKS);
        given(documentInfoMapper.readInfo(any(InputStream.class))).willReturn(publik());

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
        //given(repo.getLatestRevision()).willReturn(new DummyRevision("revision"));
        given(repo.getData("revision", "test.meta")).willAnswer(RETURNS_MOCKS);
        given(documentInfoMapper.readInfo(any(InputStream.class))).willReturn(publik());

        //When
        val actual = permissionService.toAccess(namedUser, "test", "revision", "VIEW");

        //Then
        assertTrue(actual);
    }

    @SneakyThrows
    @Test
    public void anonymousCanNotAccessDraftRecord() {
        //Given
        given(repo.getData("revision", "test.meta")).willAnswer(RETURNS_MOCKS);
        given(documentInfoMapper.readInfo(any(InputStream.class))).willReturn(draft());

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
        given(repo.getData("revision", "test.meta")).willAnswer(RETURNS_MOCKS);
        given(documentInfoMapper.readInfo(any(InputStream.class))).willReturn(metadataInfo);

        //When
        val actual = permissionService.toAccess(namedUser, "test", "revision", "VIEW");

        //Then
        assertTrue(actual);
    }

    @Test
    public void namedUserCanViewDraftRecordWithGroupPermission() throws IOException {
        //Given
        val namedUser = populateSecurityContextHolder("username");
        val metadataInfo = draft();
        metadataInfo.addPermission(Permission.VIEW, "group0");
        //given(repo.getLatestRevision()).willReturn(new DummyRevision("revision"));
        given(repo.getData("revision", "test.meta")).willAnswer(RETURNS_MOCKS);
        given(documentInfoMapper.readInfo(any(InputStream.class))).willReturn(metadataInfo);
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
        //given(repo.getLatestRevision()).willReturn(new DummyRevision("revision"));
        given(repo.getData("revision", "test.meta")).willAnswer(RETURNS_MOCKS);
        given(documentInfoMapper.readInfo(any(InputStream.class))).willReturn(draft());
        given(groupStore.getGroups(namedUser)).willReturn(Collections.emptyList());

        //When
        val actual = permissionService.toAccess(namedUser, "test", "revision", "EDIT");

        //Then
        assertFalse(actual);
    }

    @Test
    public void editorCanEdit() {
        //Given
        populateSecurityContextHolderAndAuthentication("editor");
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
        populateSecurityContextHolderAndAuthentication("publisher", "ROLE_EIDC_PUBLISHER");

        //When
        val actual = permissionService.userCanMakePublic("eidc");

        //Then
        assertTrue(actual);
    }

    @Test
    public void publisherCanView() {
        //Given
        CatalogueUser publisher = publisherCanViewPopulateSecurityContextHolder("publisher","ROLE_EIDC_PUBLISHER");
        configDocumentInfoMapper(MetadataInfo.builder().catalogue("eidc").build());

        //When
        val actual = permissionService.toAccess(publisher, "test", "VIEW");

        //Then
        assertTrue(actual);
    }

    @Test
    public void nonPublisherCannotMakePublic() {
        //Given
        populateSecurityContextHolderAndAuthentication("editor","ROLE_EIDC_EDITOR");

        //When
        val actual = permissionService.userCanMakePublic("eidc");

        //Then
        assertFalse(actual);
    }

    @Test
    public void userWithoutEditorPermissionCannotEdit() {
        //Given
        populateSecurityContextHolderAndAuthentication("bob","CEH", "Another");
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
    public static class DummyRevision implements DataRevision<CatalogueUser> {
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
