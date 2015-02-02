package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.BDDMockito.*;
import static org.mockito.Matchers.any;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.crowd.model.CrowdGroup;
import uk.ac.ceh.gateway.catalogue.controllers.DocumentController;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Permission;

public class PermissionServiceTest {
    private final DataRepository repo = mock(DataRepository.class);
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper = mock(DocumentInfoMapper.class);
    private final GroupStore<CatalogueUser> groupStore = mock(GroupStore.class);
    private final PermissionService permissionService = new PermissionService(repo, documentInfoMapper, groupStore);
    
    @Test
    public void annonymousCanNotAccessUnknownRecord() throws IOException {
        //Given
        given(repo.getData(any(String.class), any(String.class))).willThrow(DataRepositoryException.class);
        
        //When
        boolean actual = permissionService.toAccess(CatalogueUser.PUBLIC_USER, "test", "a63fe7", "VIEW");
        
        //Then
        assertThat("Annonymous user should not be able to access unknown record", actual, equalTo(false));
    }
    
    @Test
    public void annonymousCanAccessPublicRecord() throws IOException {
        //Given
        given(repo.getData("a63fe7", "test.meta")).willAnswer(RETURNS_MOCKS);
        given(documentInfoMapper.readInfo(any(InputStream.class))).willReturn(new MetadataInfo().setState("public"));
        
        //When
        boolean actual = permissionService.toAccess(CatalogueUser.PUBLIC_USER, "test", "a63fe7", "VIEW");
        
        //Then
        assertThat("Annonymous user should be able to access public record", actual, equalTo(true));
    }
    
    @Test
    public void namedUserCanAccessPublicRecord() throws IOException {
        //Given
        given(repo.getData("a63fe7", "test.meta")).willAnswer(RETURNS_MOCKS);
        given(documentInfoMapper.readInfo(any(InputStream.class))).willReturn(new MetadataInfo().setState("public"));
        CatalogueUser namedUser = new CatalogueUser().setUsername("named");
        
        //When
        boolean actual = permissionService.toAccess(namedUser, "test", "a63fe7", "VIEW");
        
        //Then
        assertThat("Named user should be able to access public record", actual, equalTo(true));
    }
    
    @Test
    public void annonymousCanNotAccessDraftRecord() throws IOException {
        //Given
        given(repo.getData("a63fe7", "test.meta")).willAnswer(RETURNS_MOCKS);
        given(documentInfoMapper.readInfo(any(InputStream.class))).willReturn(new MetadataInfo().setState("draft"));
        
        //When
        boolean actual = permissionService.toAccess(CatalogueUser.PUBLIC_USER, "test", "a63fe7", "VIEW");
        
        //Then
        assertThat("Annonymous user should not be able to access draft record", actual, equalTo(false));
    }
    
    @Test
    public void namedUserCanAccessDraftRecordWithUsernamePermission() throws IOException {
        //Given
        given(repo.getData("a63fe7", "test.meta")).willAnswer(RETURNS_MOCKS);
        MetadataInfo metadataInfo = new MetadataInfo().setState("draft");
        metadataInfo.addPermission(Permission.VIEW, "username");
        given(documentInfoMapper.readInfo(any(InputStream.class))).willReturn(metadataInfo);
        CatalogueUser namedUser = new CatalogueUser().setUsername("username");
        
        //When
        boolean actual = permissionService.toAccess(namedUser, "test", "a63fe7", "VIEW");
        
        //Then
        assertThat("Named user should be able to access draft record through username", actual, equalTo(true));
    }
    
    @Test
    public void namedUserCanViewDraftRecordWithGroupPermission() throws IOException {
        //Given
        CatalogueUser namedUser = new CatalogueUser().setUsername("username");
        Group group0 = new CrowdGroup("group0");
        given(repo.getData("a63fe7", "test.meta")).willAnswer(RETURNS_MOCKS);
        MetadataInfo metadataInfo = new MetadataInfo().setState("draft");
        metadataInfo.addPermission(Permission.VIEW, "group0");
        given(documentInfoMapper.readInfo(any(InputStream.class))).willReturn(metadataInfo);
        given(groupStore.getGroups(namedUser)).willReturn(Arrays.asList(group0));
        
        //When
        boolean actual = permissionService.toAccess(namedUser, "test", "a63fe7", "VIEW");
        
        //Then
        assertThat("Named user should be able to access draft record through group", actual, equalTo(true));
    }
    
    @Test
    public void namedUserCannotWriteDraftRecordWithNoGroupPermission() throws IOException {
        //Given
        CatalogueUser namedUser = new CatalogueUser().setUsername("username");
        given(repo.getData("a63fe7", "test.meta")).willAnswer(RETURNS_MOCKS);
        MetadataInfo metadataInfo = new MetadataInfo().setState("draft");
        given(documentInfoMapper.readInfo(any(InputStream.class))).willReturn(metadataInfo);
        given(groupStore.getGroups(namedUser)).willReturn(Collections.EMPTY_LIST);
        
        //When
        boolean actual = permissionService.toAccess(namedUser, "test", "a63fe7", "EDIT");
        
        //Then
        assertThat("Named user should not be able to write draft record", actual, equalTo(false));
    }
    
    @Test
    public void authorCanAccessDraftRecord() throws IOException {
        //Given
        CatalogueUser author = new CatalogueUser().setUsername("author");
        given(repo.getData("a63fe7", "test.meta")).willAnswer(RETURNS_MOCKS);
        MetadataInfo metadataInfo = new MetadataInfo().setState("draft");
        given(documentInfoMapper.readInfo(any(InputStream.class))).willReturn(metadataInfo);
        DataRevision<CatalogueUser> dataRevision = createDataRevision("author");
        given(repo.getRevisions(any(String.class))).willReturn(Arrays.asList(dataRevision));
        
        //When
        boolean actual = permissionService.toAccess(author, "test", "a63fe7", "VIEW");
        
        //Then
        assertThat("Author should be able to access draft record that they are author of", actual, equalTo(true));
    }
    
    @Test
    public void editorCanEdit() {
        //Given
        CatalogueUser editor = new CatalogueUser().setUsername("editor");
        Group editorRole = new CrowdGroup(DocumentController.EDITOR_ROLE);
        given(groupStore.getGroups(editor)).willReturn(Arrays.asList(editorRole));
        
        //When
        boolean actual = permissionService.userCanEdit(editor);
        
        //Then
        assertThat("Editor should be able to edit", actual, equalTo(true));
    }
    
    @Test
    public void userWithoutEditorRoleCannotEdit() {
        //Given
        CatalogueUser user = new CatalogueUser().setUsername("bob");
        Group group0 = new CrowdGroup("CEH");
        Group group1 = new CrowdGroup("Another");
        given(groupStore.getGroups(user)).willReturn(Arrays.asList(group0, group1));
        
        //When
        boolean actual = permissionService.userCanEdit(user);
        
        //Then
        assertThat("Editor should be able to edit", actual, equalTo(false));
    }
    
    @Test
    public void publicCannotEdit() {
        //Given
        given(groupStore.getGroups(CatalogueUser.PUBLIC_USER)).willReturn(Collections.EMPTY_LIST);
        
        //When
        boolean actual = permissionService.userCanEdit(CatalogueUser.PUBLIC_USER);
        
        //Then
        assertThat("Public should not be able to edit", actual, equalTo(false));
    }
    
    private DataRevision<CatalogueUser> createDataRevision(String username) {
        return new DataRevision<CatalogueUser>() {

            @Override
            public String getRevisionID() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getMessage() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getShortMessage() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CatalogueUser getAuthor() {
                return new CatalogueUser().setUsername(username);
            }
        };
    }

}