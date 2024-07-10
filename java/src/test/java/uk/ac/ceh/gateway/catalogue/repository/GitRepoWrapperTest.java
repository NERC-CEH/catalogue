package uk.ac.ceh.gateway.catalogue.repository;

import com.google.common.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.DataOngoingCommit;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataWriter;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;

import java.io.OutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class GitRepoWrapperTest {
    @Mock private DataRepository<CatalogueUser> repo;
    @Mock private DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    @Mock private BundledReaderService<MetadataDocument> bundledReader;
    @Mock private EventBus eventBus;

    @InjectMocks private GitRepoWrapper repoWrapper;

    @Test
    public void canSave() throws DataRepositoryException {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
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
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        String id = "test";

        given(repo.deleteData("test.meta")).willReturn(dataOngoingCommit);
        given(dataOngoingCommit.deleteData("test.raw")).willReturn(dataOngoingCommit);

        //When
        repoWrapper.delete(user, id);

        //Then
        verify(dataOngoingCommit).commit(user, "delete document: test");
    }

}
