package uk.ac.ceh.gateway.catalogue.document.reading;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.UnknownContentTypeException;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MetadataInfoBundledReaderServiceTest {
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) CachedDataRepository cachedRepo;
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

        doReturn("meta".getBytes()).when(cachedRepo).readAtRevision(revision, "file.meta");
        doReturn("file".getBytes()).when(cachedRepo).readAtRevision(revision, "file.raw");

        MetadataInfo metadata = MetadataInfo.builder().rawType("text/xml").build();
        doReturn(GeminiDocument.class).when(representationService).getType(any(String.class));
        doReturn(metadata).when(documentInfoMapper).readInfo(any(InputStream.class));

        GeminiDocument geminiDocument = mock(GeminiDocument.class);
        doReturn(geminiDocument).when(documentReader).read(any(InputStream.class), eq(MediaType.TEXT_XML), eq(GeminiDocument.class));
        doReturn(uri).when(documentIdentifierService).generateUri(fileToRead, revision);

        //When
        service.readBundle(fileToRead, revision);

        //Then
        verify(geminiDocument).setMetadata(any(MetadataInfo.class));
        verify(geminiDocument).setUri(uri);
        verify(postProcessingService).postProcess(geminiDocument);
    }
}
