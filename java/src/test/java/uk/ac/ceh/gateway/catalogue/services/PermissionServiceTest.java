package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.io.InputStream;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.BDDMockito.*;
import static org.mockito.Matchers.any;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

public class PermissionServiceTest {
    private final DataRepository repo = mock(DataRepository.class);
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper = mock(DocumentInfoMapper.class);
    private final PermissionService permissionService = new PermissionService(repo, documentInfoMapper);
    
    @Test
    public void annonymousCanAccessPublicRecord() throws IOException {
        //Given
        given(repo.getData("a63fe7", "test.meta")).willAnswer(RETURNS_MOCKS);
        given(documentInfoMapper.readInfo(any(InputStream.class))).willReturn(new MetadataInfo().setState("public"));
        
        //When
        boolean actual = permissionService.toAccess(CatalogueUser.PUBLIC_USER, "test", "a63fe7", "DOCUMENT_READ");
        
        //Then
        assertThat("Annonymous user should be able to access public record", actual, equalTo(true));
    }
    
    @Test
    public void annonymousCanNotAccessDraftRecord() throws IOException {
        //Given
        given(repo.getData("a63fe7", "test.meta")).willAnswer(RETURNS_MOCKS);
        given(documentInfoMapper.readInfo(any(InputStream.class))).willReturn(new MetadataInfo().setState("draft"));
        
        //When
        boolean actual = permissionService.toAccess(CatalogueUser.PUBLIC_USER, "test", "a63fe7", "DOCUMENT_READ");
        
        //Then
        assertThat("Annonymous user should not be able to access draft record", actual, equalTo(false));
    }

}