package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.io.InputStream;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.mockito.Matchers.*;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataWriter;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DocumentDoesNotExistException;
import uk.ac.ceh.gateway.catalogue.model.State;

public class GitPublicationServiceTest {
    @Spy DataRepository<CatalogueUser> repo;
    @Mock DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    @Mock StateAssembler stateAssembler;
    
    CatalogueUser editor;
    String filename = "e5090602-6ff9-4936-8217-857ea6de5774";
    
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();
    
    @Before
    public void given() throws IOException {
        editor = new CatalogueUser().setUsername("Ron MacDonald").setEmail("ron@example.com");
        repo = new GitDataRepository(folder.getRoot(),
                                     new InMemoryUserStore<>(),
                                     new AnnotatedUserHelper(CatalogueUser.class),
                                     new EventBus());
        
        repo.submitData(filename + ".meta", (o)->{})
            .commit(editor, "Uploading files");
        
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void successfullyTransitionState() throws DataRepositoryException, IOException {
        //Given
        GitPublicationService publicationService = new GitPublicationService(repo, documentInfoMapper, stateAssembler);
        when(documentInfoMapper.readInfo(any(InputStream.class))).thenReturn(new MetadataInfo("test", "", "draft"));
        when(stateAssembler.toResource(editor, filename, "draft")).thenReturn(State.builder().id("draft").build());
        
        //When
        final State transition = publicationService.transition(editor, filename, "pending");
        
        //Then
        verify(repo).getData(filename + ".meta");
        verify(documentInfoMapper).readInfo(any(InputStream.class));
        verify(repo).submitData(any(String.class), any(DataWriter.class));
        assertThat("State Id should be pending", transition.getId(), equalTo("pending"));
    }
    
    @Test
    public void successfullyGetCurrentState() throws DataRepositoryException, IOException {
        //Given 
        GitPublicationService publicationService = new GitPublicationService(repo, documentInfoMapper, stateAssembler);
        when(documentInfoMapper.readInfo(any(InputStream.class))).thenReturn(new MetadataInfo("test", "", "draft"));
        when(stateAssembler.toResource(editor, filename, "draft")).thenReturn(State.builder().id("draft").build());
        
        //When
        State current = publicationService.current(editor, filename);
        
        //Then
        verify(repo).getData(filename + ".meta");
        verify(documentInfoMapper).readInfo(any(InputStream.class));
        verify(stateAssembler).toResource(editor, filename, "draft");
        assertThat("State is should be draft", current.getId(), equalTo("draft"));
    }
    
    @Test(expected = DocumentDoesNotExistException.class)
    public void tryToGetFileThatDoesNotExist() {
        //Given 
        GitPublicationService publicationService = new GitPublicationService(repo, documentInfoMapper, stateAssembler);
        
        //When
        publicationService.current(editor, "this file name does not exist");
        
        //Then
        //The expected Exception should be thrown
    }

}