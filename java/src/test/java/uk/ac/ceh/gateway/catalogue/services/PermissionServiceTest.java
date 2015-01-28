package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.BDDMockito.*;
import static org.mockito.Matchers.any;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.crowd.model.CrowdGroup;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Permission;

public class PermissionServiceTest {
    private final DataRepository repo = mock(DataRepository.class);
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper = mock(DocumentInfoMapper.class);
    private final GroupStore<CatalogueUser> groupStore = mock(GroupStore.class);
    private final PermissionService permissionService = new PermissionService(repo, documentInfoMapper, groupStore);
    
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
    public void namedUserCanAccessDraftRecordWithGroupPermission() throws IOException {
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

}