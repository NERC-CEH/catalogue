package uk.ac.ceh.gateway.catalogue.document.reading;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.git.GitDataDocument;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.UnknownContentTypeException;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MetadataInfoBundledReaderServiceTest {
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DataRepository<CatalogueUser> repo;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS)
    DocumentReadingService documentReader;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS)
    DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS)
    DocumentTypeLookupService representationService;
    @Mock PostProcessingService postProcessingService;
    @Mock
    DocumentIdentifierService documentIdentifierService;
    @InjectMocks private MetadataInfoBundledReaderService service;

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
