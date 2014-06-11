package uk.ac.ceh.gateway.catalogue.controllers;

import com.google.common.eventbus.EventBus;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Answers;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.datastore.git.GitDataDocument;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.DocumentBundleService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoFactory;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.PublicationService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
public class DocumentControllerTest {
    
    @Spy DataRepository<CatalogueUser> repo;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentReadingService<GeminiDocument> documentReader;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentInfoMapper documentInfoMapper;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentInfoFactory<GeminiDocument, MetadataInfo> infoFactory;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentBundleService<GeminiDocument, MetadataInfo> documentBundler;
    @Mock PublicationService publicationService;
    private DocumentController controller;
    
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();
    
    @Before
    public void initMocks() throws IOException {
        repo = new GitDataRepository(folder.getRoot(),
                                     new InMemoryUserStore<>(),
                                     new AnnotatedUserHelper(CatalogueUser.class),
                                     new EventBus());
        MockitoAnnotations.initMocks(this);
        controller = new DocumentController(repo,
                                            documentReader,
                                            documentInfoMapper,
                                            infoFactory,
                                            documentBundler,
                                            publicationService);
    }
    
    @Test
    public void uploadingDocumentStoresInputStreamIntoGit() throws IOException, UnknownContentTypeException {
        //Given
        byte[] contentToUpload = "geminidocument".getBytes();
        byte[] metaInfoBytes = "metadataInfo".getBytes();
        String uploadMessage = "My upload message";
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getInputStream()).thenReturn(new DelegatedServletInputStream(new ByteArrayInputStream(contentToUpload)));
        
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn("id");
        when(documentReader.read(any(InputStream.class), eq(MediaType.APPLICATION_JSON))).thenReturn(document);
        
        MetadataInfo metadataDocument = mock(MetadataInfo.class);
        when(infoFactory.createInfo(eq(document), eq(MediaType.APPLICATION_JSON))).thenReturn(metadataDocument);
        doAnswer((Answer) (InvocationOnMock invocation) -> {
            OutputStream out = (OutputStream)invocation.getArguments()[1];
            StreamUtils.copy(metaInfoBytes, out);
            return null;
        }).when(documentInfoMapper).writeInfo(eq(metadataDocument), any(OutputStream.class));
                
        //When
        CatalogueUser user = new CatalogueUser();
        user.setUsername("user");
        user.setEmail("user@test.com");
        controller.uploadDocument(user, uploadMessage, request, MediaType.APPLICATION_JSON_VALUE);
        
        //Then
        byte[] rawWrittenData = StreamUtils.copyToByteArray(repo.getData("id.raw").getInputStream());
        byte[] rawMetaInfo = StreamUtils.copyToByteArray(repo.getData("id.meta").getInputStream());
        assertArrayEquals("Expected the data written to the repo to the same as the raw type", contentToUpload, rawWrittenData);
        assertArrayEquals("Expected the data metaInfo writen to the repo to the same as the read data", metaInfoBytes, rawMetaInfo);
        assertEquals("Expected the user to be the author of the commit", user, lastCommit("id.meta").getAuthor());
        assertEquals("Got wrong upload message", uploadMessage, lastCommit("id.meta").getMessage());
        assertEquals("Expected the user to be the author of the commit", user, lastCommit("id.raw").getAuthor());
        assertEquals("Got wrong upload message", uploadMessage, lastCommit("id.raw").getMessage());
    }
    
    @Test
    public void checkCanDeleteAFile() throws IOException {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("user");
        user.setEmail("user@test.com");
        repo.submitData("id.meta", (o)->{})
            .submitData("id.raw", (o)->{})
            .commit(user, "Uploading files");
        
        //When
        controller.deleteDocument(user, "documents no longer wanted", "id");
        
        //Then
        assertTrue("Didn't expect any files in git", repo.getFiles().isEmpty());
        assertEquals("Expected the user to be the author of the commit", user, lastCommit("id.meta").getAuthor());
        assertEquals("Got wrong upload message", "Uploading files", lastCommit("id.meta").getMessage());
        assertEquals("Expected the user to be the author of the commit", user, lastCommit("id.raw").getAuthor());
        assertEquals("Got wrong upload message", "Uploading files", lastCommit("id.raw").getMessage());
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
        when(documentInfoMapper.readInfo(metadataInfoInputStream)).thenReturn(metadata);
        
        GeminiDocument geminiDocument = mock(GeminiDocument.class);
        when(documentReader.read(rawInputStream, MediaType.TEXT_XML)).thenReturn(geminiDocument);
        
        //When
        controller.readMetadata(fileToRead, revision);
        
        //Then
        verify(documentBundler).bundle(geminiDocument, metadata);
    }
    
    @Test public void userCanGetCurrentPublicationState() {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("user");
        user.setEmail("user@test.com");
        
        String file = "123-123-123-123";
        
        //when
        controller.currentPublication(user, file);
        
        //Then
        verify(publicationService).current(user, file);
    }
    
    @Test public void userCanTransitionPublicationState() {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("user");
        user.setEmail("user@test.com");
        
        String file = "123-123-123-123";
        String state = "pending";
        
        //when
        controller.transitionPublication(user, file, state);
        
        //Then
        verify(publicationService).transition(user, file, state); 
    }
    
    private DataRevision<CatalogueUser> lastCommit(String file) throws DataRepositoryException {
        List<DataRevision<CatalogueUser>> revisions = repo.getRevisions(file);
        return revisions.get(revisions.size()-1);
    }
}
