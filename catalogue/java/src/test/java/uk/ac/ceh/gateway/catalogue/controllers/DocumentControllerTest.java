package uk.ac.ceh.gateway.catalogue.controllers;

import com.google.common.eventbus.EventBus;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.StreamUtils;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import uk.ac.ceh.gateway.catalogue.model.Citation;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.CitationService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoFactory;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
public class DocumentControllerTest {
    
    @Spy DataRepository<CatalogueUser> repo;
    @Mock DocumentIdentifierService documentIdentifierService;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentReadingService documentReader;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentInfoMapper documentInfoMapper;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) DocumentInfoFactory<MetadataDocument, MetadataInfo> infoFactory;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) BundledReaderService<MetadataDocument> documentBundleReader;
    @Mock DocumentLinkService linkService;
    @Mock CitationService citationService;
    @Mock DocumentWritingService<MetadataDocument> documentWritingService;
    
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
        controller = spy(new DocumentController(repo,
                                                documentIdentifierService,
                                                documentReader,
                                                documentInfoMapper,
                                                infoFactory,
                                                documentBundleReader,
                                                documentWritingService,
                                                linkService,
                                                citationService));
    }
    
    private HttpServletRequest mockRequest() {
        return new MockHttpServletRequest();
    }
    
    @Test
    public void uploadingDocumentStoresInputStreamIntoGit() throws IOException, UnknownContentTypeException, DataRepositoryException, DocumentIndexingException {
        //Given
        byte[] contentToUpload = "geminidocument".getBytes();
        byte[] metaInfoBytes = "metadataInfo".getBytes();
        String uploadMessage = "My upload message";
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getInputStream()).thenReturn(new DelegatedServletInputStream(new ByteArrayInputStream(contentToUpload)));
        when(request.getContextPath()).thenReturn("/documents");
        when(request.getScheme()).thenReturn("https");
        when(request.getServerName()).thenReturn("example.com");
        when(request.getServerPort()).thenReturn(443);
        
        GeminiDocument document = new GeminiDocument();
        document.setId("id");
        when(documentIdentifierService.generateFileId("id")).thenReturn("id");
        when(documentReader.read(any(InputStream.class), eq(MediaType.APPLICATION_JSON), eq(GeminiDocument.class))).thenReturn(document);
        
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
    public void checkThatReadingDelegatesToBundledReadingService() throws IOException, DataRepositoryException, UnknownContentTypeException {
        //Given
        GeminiDocument bundledDocument = new GeminiDocument();
        bundledDocument.setMetadata(new MetadataInfo().setState("public").setDocumentType("GEMINI_DOCUMENT"));
        
        String file = "myFile";       
        String latestRevisionId = "latestRev";
        
        DataRevision revision = mock(DataRevision.class);
        when(revision.getRevisionID()).thenReturn(latestRevisionId);
        doReturn(revision).when(repo).getLatestRevision();
        
        when(documentBundleReader.readBundle(file, latestRevisionId)).thenReturn(bundledDocument);
        doReturn(null).when(controller).getCurrentUri(any(HttpServletRequest.class), any(String.class), any(String.class));
        when(linkService.getParent(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(linkService.getRevised(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(linkService.getRevisionOf(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(citationService.getCitation(any(GeminiDocument.class))).thenReturn(Optional.empty());
        
        //When
        MetadataDocument readDocument = controller.readMetadata(CatalogueUser.PUBLIC_USER, file, latestRevisionId, mockRequest());
        
        //Then
        verify(documentBundleReader).readBundle(file, latestRevisionId);
        assertEquals("Expected the mocked gemini document", bundledDocument, readDocument);
    }
    
    @Test
    public void checkThatLinksAreAddedToDataset() throws IOException, DataRepositoryException, UnknownContentTypeException {
        //Given
        GeminiDocument bundledDocument = new GeminiDocument();
        bundledDocument.setMetadata(new MetadataInfo().setState("public").setDocumentType("GEMINI_DOCUMENT"));
        bundledDocument.setResourceType(Keyword.builder().value("dataset").build());
        
        String file = "myFile";
        String latestRevisionId = "latestRev";
        
        DataRevision revision = mock(DataRevision.class);
        when(revision.getRevisionID()).thenReturn(latestRevisionId);
        doReturn(revision).when(repo).getLatestRevision();
        
        when(documentBundleReader.readBundle(file, latestRevisionId)).thenReturn(bundledDocument);
        doReturn(null).when(controller).getCurrentUri(any(HttpServletRequest.class), any(String.class), any(String.class));
        when(linkService.getParent(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(linkService.getRevised(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(linkService.getRevisionOf(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(citationService.getCitation(any(GeminiDocument.class))).thenReturn(Optional.empty());
        //When
        controller.readMetadata(CatalogueUser.PUBLIC_USER, file, mockRequest());
        
        //Then
        verify(documentBundleReader).readBundle(file, latestRevisionId);
        verify(linkService).getLinks(any(GeminiDocument.class), any(String.class));
        verify(linkService).getParent(any(GeminiDocument.class), any(String.class));
        verify(linkService).getChildren(any(GeminiDocument.class), any(String.class));
    }
    
    @Test
    public void checkThatReadingLatestFileComesFromLatestRevision() throws IOException, DataRepositoryException, UnknownContentTypeException {
        //Given
        String latestRevisionId = "latestRev";
        String file = "myFile";
        
        GeminiDocument bundledDocument = new GeminiDocument();
        bundledDocument.setMetadata(new MetadataInfo().setState("public").setDocumentType("GEMINI_DOCUMENT"));
        when(documentBundleReader.readBundle(file, latestRevisionId)).thenReturn(bundledDocument);
        
        DataRevision revision = mock(DataRevision.class);
        when(revision.getRevisionID()).thenReturn(latestRevisionId);
        doReturn(revision).when(repo).getLatestRevision();
        when(linkService.getParent(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(linkService.getRevised(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(linkService.getRevisionOf(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(citationService.getCitation(any(GeminiDocument.class))).thenReturn(Optional.empty());
        
        //When
        controller.readMetadata(CatalogueUser.PUBLIC_USER, file, mockRequest());
        
        //Then
        verify(documentBundleReader).readBundle(file, latestRevisionId);
    }
    
    @Test
    public void checkThatReadingLatestFileDelegatesToReadingService() throws IOException, DataRepositoryException, UnknownContentTypeException {
        //Given
        String latestRevisionId = "latestRev";
        String file = "myFile";
        
        GeminiDocument bundledDocument = new GeminiDocument();
        bundledDocument.setMetadata(new MetadataInfo().setState("public").setDocumentType("GEMINI_DOCUMENT"));
        when(documentBundleReader.readBundle(file, latestRevisionId)).thenReturn(bundledDocument);
        
        DataRevision revision = mock(DataRevision.class);
        when(revision.getRevisionID()).thenReturn(latestRevisionId);
        doReturn(revision).when(repo).getLatestRevision();
        when(linkService.getParent(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(linkService.getRevised(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(linkService.getRevisionOf(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(citationService.getCitation(any(GeminiDocument.class))).thenReturn(Optional.empty());
        
        //When
        MetadataDocument readDocument = controller.readMetadata(CatalogueUser.PUBLIC_USER, file, mockRequest());
        
        //Then
        verify(documentBundleReader).readBundle(eq(file), any(String.class));
        assertEquals("Expected the mocked gemini document", bundledDocument, readDocument);
    }
    
    @Test
    public void checkThatURIsAttachedToDocumentOnReading() throws IOException, UnknownContentTypeException, URISyntaxException {
        //Given        
        GeminiDocument bundledDocument = mock(GeminiDocument.class);
        URI uri = new URI("http://whatever.com");
        doReturn(uri).when(controller).getCurrentUri(any(HttpServletRequest.class), any(String.class), any(String.class));
        when(documentBundleReader.readBundle(any(String.class), any(String.class))).thenReturn(bundledDocument);
        
        String latestRevisionId = "latestRev";
        
        DataRevision revision = mock(DataRevision.class);
        when(revision.getRevisionID()).thenReturn(latestRevisionId);
        doReturn(revision).when(repo).getLatestRevision();
        when(linkService.getParent(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(linkService.getRevised(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(linkService.getRevisionOf(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(citationService.getCitation(any(GeminiDocument.class))).thenReturn(Optional.empty());
        
        //When
        controller.readMetadata(CatalogueUser.PUBLIC_USER, "file", latestRevisionId, mockRequest());
        
        //Then
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(bundledDocument).attachUri(uriCaptor.capture());
        assertSame("Expected the same uri", uri, uriCaptor.getValue());
    }
    
    @Test
    public void checkThatCitationIsPopulatedForGeminiDocument() throws IOException, UnknownContentTypeException {
        //Given
        GeminiDocument bundledDocument = mock(GeminiDocument.class);
        Optional<Citation> citation = Optional.of(Citation.builder().build());
        when(documentBundleReader.readBundle(any(String.class), any(String.class))).thenReturn(bundledDocument);
        when(citationService.getCitation(bundledDocument)).thenReturn(citation);
        doReturn(null).when(controller).getCurrentUri(any(HttpServletRequest.class), any(String.class), any(String.class));
        
        String latestRevisionId = "latestRev";
        
        DataRevision revision = mock(DataRevision.class);
        when(revision.getRevisionID()).thenReturn(latestRevisionId);
        doReturn(revision).when(repo).getLatestRevision();
        when(linkService.getParent(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(linkService.getRevised(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        when(linkService.getRevisionOf(any(GeminiDocument.class), any(String.class))).thenReturn(Optional.empty());
        
        //When
        controller.readMetadata(CatalogueUser.PUBLIC_USER, "file", latestRevisionId, mockRequest());
        
        //Then
        verify(bundledDocument).setCitation(citation.get());
    }
    
    @Test
    public void checkThatCurrentURIIsNonHistoricForLatestRevision() throws DataRepositoryException {
        //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String file = "file";
        String revisionId = "latest";
        
        DataRevision revision = mock(DataRevision.class);
        when(revision.getRevisionID()).thenReturn(revisionId);
        doReturn(revision).when(repo).getLatestRevision();
        
        //When
        URI docUri = controller.getCurrentUri(request, file, revisionId);
        
        //Then
        assertThat("Expected latest uri", docUri.toString(), equalTo("http://localhost/id/file"));
    }
    
    @Test
    public void checkThatCurrentURIIsHistoricForOtherRevision() throws DataRepositoryException {
         //Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String file = "file";
        String revisionId = "inThePast";
        
        DataRevision revision = mock(DataRevision.class);
        when(revision.getRevisionID()).thenReturn("latest");
        doReturn(revision).when(repo).getLatestRevision();
        
        //When
        URI docUri = controller.getCurrentUri(request, file, revisionId);
        
        //Then
        assertThat("Expected latest uri", docUri.toString(), equalTo("http://localhost/history/inThePast/file"));
    }
    
    private DataRevision<CatalogueUser> lastCommit(String file) throws DataRepositoryException {
        List<DataRevision<CatalogueUser>> revisions = repo.getRevisions(file);
        return revisions.get(revisions.size()-1);
    }
}