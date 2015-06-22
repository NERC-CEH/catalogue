package uk.ac.ceh.gateway.catalogue.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.git.GitDataDocument;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

/**
 *
 * @author cjohn
 */
public class MetadataInfoBundledReaderServiceTest {    
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DataRepository<CatalogueUser> repo;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentReadingService documentReader;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentInfoMapper documentInfoMapper;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentInfoFactory<GeminiDocument, MetadataInfo> infoFactory;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentTypeLookupService representationService;
    private MetadataInfoBundledReaderService service;
    
    
    @Before
    public void initMocks() throws IOException {
        MockitoAnnotations.initMocks(this);
        service = new MetadataInfoBundledReaderService(repo,
                                            documentReader,
                                            documentInfoMapper,
                                            representationService);
    }
    
    @Test
    public void checkDocumentIsBundledWhenReadFromParticularRevision() throws DataRepositoryException, IOException, UnknownContentTypeException {
        //Given
        String fileToRead = "file";
        String revision = "HEAD";
        
        ByteArrayInputStream metadataInfoInputStream = new ByteArrayInputStream("meta".getBytes());
        GitDataDocument metadataInfoDocument = mock(GitDataDocument.class);
        when(metadataInfoDocument.getInputStream()).thenReturn(metadataInfoInputStream);
        
        ByteArrayInputStream rawInputStream = new ByteArrayInputStream("file".getBytes());
        GitDataDocument rawDocument = mock(GitDataDocument.class);
        when(rawDocument.getInputStream()).thenReturn(rawInputStream);
        
        doReturn(metadataInfoDocument).when(repo).getData(revision, "file.meta");
        doReturn(rawDocument).when(repo).getData(revision, "file.raw");
        
        MetadataInfo metadata = mock(MetadataInfo.class);
        when(metadata.getRawMediaType()).thenReturn(MediaType.TEXT_XML);
        when(representationService.getType(any(String.class))).thenReturn((Class)GeminiDocument.class);
        when(documentInfoMapper.readInfo(metadataInfoInputStream)).thenReturn(metadata);
        
        GeminiDocument geminiDocument = mock(GeminiDocument.class);
        when(documentReader.read(rawInputStream, MediaType.TEXT_XML, GeminiDocument.class)).thenReturn(geminiDocument);
        
        //When
        service.readBundle(fileToRead, revision);
        
        //Then
        verify(geminiDocument).attachMetadata(metadata);
    }
}