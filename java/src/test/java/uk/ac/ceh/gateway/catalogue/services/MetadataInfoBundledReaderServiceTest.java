package uk.ac.ceh.gateway.catalogue.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import static org.mockito.ArgumentMatchers.any;
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
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;

public class MetadataInfoBundledReaderServiceTest {    
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DataRepository<CatalogueUser> repo;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentReadingService documentReader;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentTypeLookupService representationService;
    @Mock PostProcessingService postProcessingService;
    @Mock DocumentIdentifierService documentIdentifierService;
    private MetadataInfoBundledReaderService service;
    
    
    @BeforeEach
    public void initMocks() throws IOException {
        MockitoAnnotations.initMocks(this);
        service = new MetadataInfoBundledReaderService(repo,
                                            documentReader,
                                            documentInfoMapper,
                                            representationService,
                                            postProcessingService,
                                            documentIdentifierService);
    }
    
    @Test
    public void checkDocumentIsBundledWhenReadFromParticularRevision() throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        //Given
        String fileToRead = "file";
        String revision = "HEAD";
        String uri = "http://example.com/file";
        
        ByteArrayInputStream metadataInfoInputStream = new ByteArrayInputStream("meta".getBytes());
        GitDataDocument metadataInfoDocument = mock(GitDataDocument.class);
        when(metadataInfoDocument.getInputStream()).thenReturn(metadataInfoInputStream);
        
        ByteArrayInputStream rawInputStream = new ByteArrayInputStream("file".getBytes());
        GitDataDocument rawDocument = mock(GitDataDocument.class);
        when(rawDocument.getInputStream()).thenReturn(rawInputStream);
        
        doReturn(metadataInfoDocument).when(repo).getData(revision, "file.meta");
        doReturn(rawDocument).when(repo).getData(revision, "file.raw");
        
        MetadataInfo metadata = MetadataInfo.builder().rawType("text/xml").build();
        doReturn(GeminiDocument.class).when(representationService).getType(any(String.class));
        doReturn(metadata).when(documentInfoMapper).readInfo(metadataInfoInputStream);

        GeminiDocument geminiDocument = mock(GeminiDocument.class);
        doReturn(geminiDocument).when(documentReader).read(rawInputStream, MediaType.TEXT_XML, GeminiDocument.class);
        doReturn(uri).when(documentIdentifierService).generateUri(fileToRead, revision);
        
        //When
        service.readBundle(fileToRead, revision);
        
        //Then
        verify(geminiDocument).setMetadata(any(MetadataInfo.class));
        verify(geminiDocument).setUri(uri);
        verify(postProcessingService).postProcess(geminiDocument);
    }
}
