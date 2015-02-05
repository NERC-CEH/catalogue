package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataWriter;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import uk.ac.ceh.gateway.catalogue.config.PublicationConfig;
import uk.ac.ceh.gateway.catalogue.controllers.DocumentController;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DocumentDoesNotExistException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.publication.Workflow;

public class GitPublicationServiceTest {
    @Mock GroupStore<CatalogueUser> groupStore;
    @Spy DataRepository<CatalogueUser> repo;
    @Mock DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    @Mock BundledReaderService<MetadataDocument> documentBundleReader;
    UriComponentsBuilder uriBuilder;
    Workflow workflow;
    CatalogueUser editor;
    private static final String FILENAME = "e5090602-6ff9-4936-8217-857ea6de5774";
    private static final String PENDING_ID = "ykhm7b";
    private static final String DRAFT_ID = "qtak5r";
    private MetadataDocument draft, publik;
    private GitPublicationService publicationService;
    
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();
    
    @Before
    public void given() throws IOException {
        uriBuilder = UriComponentsBuilder.fromHttpUrl("https://example.com/documents").pathSegment(FILENAME, "publication");
        workflow = new PublicationConfig().workflow();
        editor = new CatalogueUser();
        editor.setUsername("Ron MacDonald");
        editor.setEmail("ron@example.com");
        repo = new GitDataRepository(folder.getRoot(),
                                     new InMemoryUserStore<>(),
                                     new AnnotatedUserHelper(CatalogueUser.class),
                                     new EventBus());
        
        repo.submitData(FILENAME + ".meta", (o)->{})
            .commit(editor, "Uploading files");
        
        this.draft = new GeminiDocument()
            .setTitle("draft")
            .setUri(URI.create("http://localhost"))
            .setMetadata(new MetadataInfo().setState("draft"));
        
        this.publik = new GeminiDocument()
            .setTitle("public")
            .setUri(URI.create("http://localhost"))
            .setMetadata(new MetadataInfo().setState("public"));
        
        MockitoAnnotations.initMocks(this);
        
        this.publicationService = new GitPublicationService(groupStore, workflow, repo, documentInfoMapper, documentBundleReader);
    }
    
    @Test
    public void successfullyTransitionState() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Arrays.asList(createGroup(DocumentController.EDITOR_ROLE)));
        when(documentBundleReader.readBundle(any(String.class), any(String.class))).thenReturn(draft);
        when(documentInfoMapper.readInfo(any(InputStream.class))).thenReturn(draft.getMetadata());
        
        //When
        publicationService.transition(editor, FILENAME, PENDING_ID, uriBuilder);
        
        //Then
        verify(repo).submitData(any(String.class), any(DataWriter.class));
    }
    
    @Test
    public void editorCannotTransitionFromPublic() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Arrays.asList(createGroup(DocumentController.EDITOR_ROLE)));
        when(documentBundleReader.readBundle(any(String.class), any(String.class))).thenReturn(publik);
        when(documentInfoMapper.readInfo(any(InputStream.class))).thenReturn(publik.getMetadata());
        
        //When
        publicationService.transition(editor, FILENAME, DRAFT_ID, uriBuilder);
        
        //Then
        verify(repo, never()).submitData(any(String.class), any(DataWriter.class));
    }
    
    @Test
    public void publisherCanTransitionFromPublic() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Arrays.asList(createGroup(DocumentController.PUBLISHER_ROLE)));
        when(documentBundleReader.readBundle(any(String.class), any(String.class))).thenReturn(publik);
        when(documentInfoMapper.readInfo(any(InputStream.class))).thenReturn(publik.getMetadata());
        
        //When
        publicationService.transition(editor, FILENAME, DRAFT_ID, uriBuilder);
        
        //Then
        verify(repo).submitData(any(String.class), any(DataWriter.class));
    }
    
    @Test
    public void unknownCannotTransitionFromPublic() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Collections.EMPTY_LIST);
        when(documentBundleReader.readBundle(any(String.class), any(String.class))).thenReturn(publik);
        when(documentInfoMapper.readInfo(any(InputStream.class))).thenReturn(publik.getMetadata());
        
        //When
        publicationService.transition(editor, FILENAME, DRAFT_ID, uriBuilder);
        
        //Then
        verify(repo, never()).submitData(any(String.class), any(DataWriter.class));
    }
    
    @Test
    public void successfullyGetCurrentState() throws Exception {
        //Given
        when(documentBundleReader.readBundle(any(String.class), any(String.class))).thenReturn(draft);
        when(documentInfoMapper.readInfo(any(InputStream.class))).thenReturn(draft.getMetadata());
        
        //When
        StateResource current = publicationService.current(editor, FILENAME, uriBuilder);
        
        //Then
        assertThat("State is should be draft", current.getId(), equalTo("draft"));
    }
    
    @Test(expected = DocumentDoesNotExistException.class)
    public void tryToGetFileThatDoesNotExist() throws Exception {
        //Given 
        when(documentBundleReader.readBundle(any(String.class), any(String.class))).thenThrow(new DataRepositoryException("test"));
        when(documentInfoMapper.readInfo(any(InputStream.class))).thenThrow(new NullPointerException());
        
        //When
        publicationService.current(editor, "this file name does not exist", uriBuilder);
        
        //Then
        //The expected Exception should be thrown
    }
    
    private Group createGroup(String groupname) {
        return new Group() {
            @Override
            public String getName() {
                return groupname;
            }
            @Override
            public String getDescription() {
                return groupname;
            }
        };
    }
}