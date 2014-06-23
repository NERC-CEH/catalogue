package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.io.InputStream;
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
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DocumentDoesNotExistException;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.publication.Workflow;

public class GitPublicationServiceTest {
    @Mock GroupStore<CatalogueUser> groupStore;
    @Spy DataRepository<CatalogueUser> repo;
    @Mock DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    UriComponentsBuilder uriBuilder;
    Workflow workflow;
    CatalogueUser editor;
    private static final String FILENAME = "e5090602-6ff9-4936-8217-857ea6de5774";
    private static final String PENDING_ID = "ykhm7b";
    private static final String DRAFT_ID = "qtak5r";
    
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
        
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void successfullyTransitionState() throws DataRepositoryException, IOException {
        //Given
        GitPublicationService publicationService = new GitPublicationService(groupStore, workflow, repo, documentInfoMapper);
        when(documentInfoMapper.readInfo(any(InputStream.class))).thenReturn(new MetadataInfo("test", "", "draft"));
        when(groupStore.getGroups(editor)).thenReturn(Arrays.asList(new Group() {

            @Override
            public String getName() {
                return "ROLE_EDITOR";
            }

            @Override
            public String getDescription() {
                return "ROLE_EDITOR";
            }
        }));
        
        //When
        publicationService.transition(editor, FILENAME, PENDING_ID, uriBuilder);
        
        //Then
        verify(repo).submitData(any(String.class), any(DataWriter.class));
    }
    
    @Test
    public void editorCannotTransitionFromPublic() throws DataRepositoryException, IOException {
        //Given
        GitPublicationService publicationService = new GitPublicationService(groupStore, workflow, repo, documentInfoMapper);
        when(documentInfoMapper.readInfo(any(InputStream.class))).thenReturn(new MetadataInfo("test", "", "public"));
        when(groupStore.getGroups(editor)).thenReturn(Arrays.asList(new Group() {

            @Override
            public String getName() {
                return "ROLE_EDITOR";
            }

            @Override
            public String getDescription() {
                return "ROLE_EDITOR";
            }
        }));
        
        //When
        publicationService.transition(editor, FILENAME, DRAFT_ID, uriBuilder);
        
        //Then
        verify(repo, never()).submitData(any(String.class), any(DataWriter.class));
    }
    
    @Test
    public void publisherCanTransitionFromPublic() throws DataRepositoryException, IOException {
        //Given
        GitPublicationService publicationService = new GitPublicationService(groupStore, workflow, repo, documentInfoMapper);
        when(documentInfoMapper.readInfo(any(InputStream.class))).thenReturn(new MetadataInfo("test", "", "public"));
        when(groupStore.getGroups(editor)).thenReturn(Arrays.asList(new Group() {

            @Override
            public String getName() {
                return "ROLE_PUBLISHER";
            }

            @Override
            public String getDescription() {
                return "ROLE_PUBLISHER";
            }
        }));
        
        //When
        publicationService.transition(editor, FILENAME, DRAFT_ID, uriBuilder);
        
        //Then
        verify(repo).submitData(any(String.class), any(DataWriter.class));
    }
    
    @Test
    public void unknownCannotTransitionFromPublic() throws DataRepositoryException, IOException {
        //Given
        GitPublicationService publicationService = new GitPublicationService(groupStore, workflow, repo, documentInfoMapper);
        when(documentInfoMapper.readInfo(any(InputStream.class))).thenReturn(new MetadataInfo("test", "", "public"));
        when(groupStore.getGroups(editor)).thenReturn(Collections.EMPTY_LIST);
        
        //When
        publicationService.transition(editor, FILENAME, DRAFT_ID, uriBuilder);
        
        //Then
        verify(repo, never()).submitData(any(String.class), any(DataWriter.class));
    }
    
    @Test
    public void successfullyGetCurrentState() throws DataRepositoryException, IOException {
        //Given
        GitPublicationService publicationService = new GitPublicationService(groupStore, workflow, repo, documentInfoMapper);
        when(documentInfoMapper.readInfo(any(InputStream.class))).thenReturn(new MetadataInfo("test", "", "draft"));
        
        
        //When
        StateResource current = publicationService.current(editor, FILENAME, uriBuilder);
        
        //Then
        verify(repo).getData(FILENAME + ".meta");
        verify(documentInfoMapper).readInfo(any(InputStream.class));
        assertThat("State is should be draft", current.getId(), equalTo("draft"));
    }
    
    @Test(expected = DocumentDoesNotExistException.class)
    public void tryToGetFileThatDoesNotExist() {
        //Given 
        GitPublicationService publicationService = new GitPublicationService(groupStore, workflow, repo, documentInfoMapper);
        
        //When
        publicationService.current(editor, "this file name does not exist", uriBuilder);
        
        //Then
        //The expected Exception should be thrown
    }

}