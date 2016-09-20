package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.config.PublicationConfig;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.PublicationServiceException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.publication.Workflow;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

public class GitPublicationServiceTest {
    @Mock GroupStore<CatalogueUser> groupStore;
    @Mock DocumentRepository documentRepository;
    UriComponentsBuilder uriBuilder;
    Workflow workflow;
    CatalogueUser editor;
    private static final String FILENAME = "e5090602-6ff9-4936-8217-857ea6de5774";
    private static final String PENDING_ID = "ykhm7b";
    private static final String DRAFT_ID = "qtak5r";
    private MetadataDocument draft, published;
    private GitPublicationService publicationService;
    
    @Before
    public void given() throws IOException {
        uriBuilder = UriComponentsBuilder.fromHttpUrl("https://example.com/documents").pathSegment(FILENAME, "publication");
        workflow = new PublicationConfig().workflow();
        editor = new CatalogueUser()
            .setUsername("Ron MacDonald")
            .setEmail("ron@example.com");
        
        this.draft = new GeminiDocument()
            .setTitle("draft")
            .setId("3beb9650-fc88-4de5-b8cd-9cc8a4abe135")
            .setMetadata(MetadataInfo.builder().state("draft").catalogue("eidc").build());
        
        this.published = new GeminiDocument()
            .setTitle("published")
            .setId("db49a6ee-5c9e-4bef-8e6e-196387df4d97")
            .setMetadata(MetadataInfo.builder().state("published").catalogue("eidc").build());
        
        MockitoAnnotations.initMocks(this);
        
        this.publicationService = new GitPublicationService(groupStore, workflow, documentRepository);
    }
    
    @Test
    public void successfullyTransitionState() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Arrays.asList(createGroup("ROLE_EIDC_EDITOR")));
        when(documentRepository.read(FILENAME)).thenReturn(draft);
        
        //When
        publicationService.transition(editor, FILENAME, PENDING_ID, uriBuilder);
        
        //Then
        verify(documentRepository).read(FILENAME);
        verify(documentRepository).save(editor, draft, FILENAME, "Publication state of e5090602-6ff9-4936-8217-857ea6de5774 changed.");
    }
    
    @Test
    public void editorCannotTransitionFromPublic() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Arrays.asList(createGroup("ROLE_EIDC_EDITOR")));
        when(documentRepository.read(FILENAME)).thenReturn(published);
        
        //When
        StateResource actual = publicationService.transition(editor, FILENAME, DRAFT_ID, uriBuilder);
        
        //Then
        verify(documentRepository).read(FILENAME);
        verify(documentRepository).save(editor, published, FILENAME, "Publication state of e5090602-6ff9-4936-8217-857ea6de5774 changed.");
        assertThat("State should still be published", actual.getTitle(), equalTo("Published"));
    }
    
    @Test
    public void publisherCanTransitionFromPublic() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Arrays.asList(createGroup("ROLE_EIDC_PUBLISHER")));
        when(documentRepository.read(FILENAME)).thenReturn(published);
        
        //When
        StateResource actual = publicationService.transition(editor, FILENAME, DRAFT_ID, uriBuilder);
        
        //Then
        verify(documentRepository).read(FILENAME);
        verify(documentRepository).save(editor, published, FILENAME, "Publication state of e5090602-6ff9-4936-8217-857ea6de5774 changed.");
        assertThat("State should be draft", actual.getTitle(), equalTo("Draft"));
    }
    
    @Test
    public void unknownCannotTransitionFromPublic() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Collections.EMPTY_LIST);
        when(documentRepository.read(FILENAME)).thenReturn(published);
        
        //When
        StateResource actual = publicationService.transition(editor, FILENAME, DRAFT_ID, uriBuilder);
        
        //Then
        verify(documentRepository).read(FILENAME);
        verify(documentRepository).save(editor, published, FILENAME, "Publication state of e5090602-6ff9-4936-8217-857ea6de5774 changed.");
        assertThat("State should still be published", actual.getTitle(), equalTo("Published"));
    }
    
    @Test
    public void successfullyGetCurrentState() throws Exception {
        //Given
        when(documentRepository.read(FILENAME)).thenReturn(draft);
        
        //When
        StateResource current = publicationService.current(editor, FILENAME, uriBuilder);
        
        //Then
        verify(documentRepository).read(FILENAME);
        assertThat("State is should be draft", current.getId(), equalTo("draft"));
    }
    
    @Test(expected = PublicationServiceException.class)
    public void tryToGetFileThatDoesNotExist() throws Exception {
        //Given 
        when(documentRepository.read(FILENAME)).thenThrow(new PublicationServiceException("test"));
        
        //When
        publicationService.current(editor, "this file name does not exist", uriBuilder);
        
        //Then
        verify(documentRepository).read(FILENAME);
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