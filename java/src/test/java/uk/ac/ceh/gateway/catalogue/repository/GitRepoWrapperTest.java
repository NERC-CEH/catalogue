package uk.ac.ceh.gateway.catalogue.repository;

import java.io.OutputStream;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataOngoingCommit;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataWriter;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;

public class GitRepoWrapperTest {
    @Mock private DataRepository<CatalogueUser> repo;
    @Mock private DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    
    private GitRepoWrapper repoWrapper;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        repoWrapper = new GitRepoWrapper(repo, documentInfoMapper);
    }

    @Test
    public void canSave() throws DataRepositoryException {
        //Given
        CatalogueUser user = new CatalogueUser();
        String id = "test";
        String message = "template: test";
        MetadataInfo metadataInfo = MetadataInfo.builder().build();
        DataWriter dataWriter = (OutputStream out) -> {
            throw new UnsupportedOperationException("Not supported yet.");
        };
        DataOngoingCommit dataOngoingCommit = mock(DataOngoingCommit.class);
        
        given(repo.submitData(eq("test.meta"), any())).willReturn(dataOngoingCommit);
        given(dataOngoingCommit.submitData("test.raw", dataWriter)).willReturn(dataOngoingCommit);
                     
        //When
        repoWrapper.save(user, id, message, metadataInfo, dataWriter);
        
        //Then
        verify(dataOngoingCommit).commit(user, "template: test");
    }
    
    @Test
    public void canDelete() throws DataRepositoryException {
        //Given
        DataOngoingCommit dataOngoingCommit = mock(DataOngoingCommit.class);
        CatalogueUser user = new CatalogueUser();
        String id = "test";
        
        given(repo.deleteData("test.meta")).willReturn(dataOngoingCommit);
        given(dataOngoingCommit.deleteData("test.raw")).willReturn(dataOngoingCommit);
        
        //When
        repoWrapper.delete(user, id);
        
        //Then
        verify(dataOngoingCommit).commit(user, "delete document: test");
    }
    
}
