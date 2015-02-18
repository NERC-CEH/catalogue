package uk.ac.ceh.gateway.catalogue.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.junit.Test;
import org.junit.Before;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataOngoingCommit;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataWriter;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DocumentDoesNotExistException;
import uk.ac.ceh.gateway.catalogue.model.DocumentSaveException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

public class GitMetadataInfoEditingServiceTest {
    @Mock private DataRepository<CatalogueUser> repo;
    @Mock private DocumentReadingService documentReader;
    @Mock private DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    @Mock private DocumentTypeLookupService representationService;
    private MetadataInfoEditingService editingService;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        editingService = new GitMetadataInfoEditingService(repo, documentReader, documentInfoMapper, representationService);
    }
    
    @Test
    public void successfullyGetMetadataDocument() throws IOException, UnknownContentTypeException {
        //Given
        ByteArrayInputStream metadataInfoInputStream = new ByteArrayInputStream("meta".getBytes());
        DataDocument metadataInfoDocument = mock(DataDocument.class);
        given(metadataInfoDocument.getInputStream()).willReturn(metadataInfoInputStream);
        given(repo.getData("test.meta")).willReturn(metadataInfoDocument);
        MetadataInfo info = mock(MetadataInfo.class);
        given(documentInfoMapper.readInfo(metadataInfoInputStream)).willReturn(info);
        
        ByteArrayInputStream rawInputStream = new ByteArrayInputStream("file".getBytes());
        DataDocument rawDocument = mock(DataDocument.class);
        given(rawDocument.getInputStream()).willReturn(rawInputStream);
        given(repo.getData("test.raw")).willReturn(rawDocument);
        MetadataDocument metadataDocument = mock(MetadataDocument.class);
        given(documentReader.read(any(InputStream.class), any(MediaType.class), any(Class.class))).willReturn(metadataDocument);
        
        //When
        editingService.getMetadataDocument("test", URI.create("/documents/test"));
        
        //Then        
        verify(info, never()).hideMediaType();
        verify(metadataDocument).attachMetadata(info);
        verify(metadataDocument).attachUri(URI.create("/documents/test"));
    }
    
    @Test(expected = DocumentDoesNotExistException.class)
    public void unableToGetUnknownFile() throws DataRepositoryException {
        //Given
        given(repo.getData("unknown.meta")).willThrow(DataRepositoryException.class);
        
        //When
        editingService.getMetadataDocument("unknown", URI.create("/documents/test"));
        
        //Then
        // DocumentDoesNotExistException should be thrown.
    }
    
    @Test
    public void successfullySaveMetadataInfo() throws DataRepositoryException {
        //Given
        String fileIdentifier = "123-456-789";
        String commitMessage = "test commit message";
        MetadataInfo info = mock(MetadataInfo.class);
        CatalogueUser user = mock(CatalogueUser.class);
        DataOngoingCommit<CatalogueUser> ongoingCommit = mock(DataOngoingCommit.class);
        given(repo.submitData(any(String.class), any(DataWriter.class))).willReturn(ongoingCommit);
        
        //When
        editingService.saveMetadataInfo(fileIdentifier, info, user, commitMessage);
        
        //Then
        verify(repo).submitData(any(String.class), any(DataWriter.class));
        verify(ongoingCommit).commit(user, commitMessage);
    }
    
    @Test(expected = DocumentSaveException.class)
    public void unableToSaveFile() throws DataRepositoryException {
        //Given
        String fileIdentifier = "123-456-789";
        String commitMessage = "test commit message";
        MetadataInfo info = mock(MetadataInfo.class);
        CatalogueUser user = mock(CatalogueUser.class);
        DataOngoingCommit<CatalogueUser> ongoingCommit = mock(DataOngoingCommit.class);
        given(ongoingCommit.commit(any(CatalogueUser.class), any(String.class))).willThrow(new DataRepositoryException("test"));
        given(repo.submitData(any(String.class), any(DataWriter.class))).willReturn(ongoingCommit);
        
        //When
        editingService.saveMetadataInfo(fileIdentifier, info, user, commitMessage);
        
        //Then
        // DocumentSaveException
    }
    
}