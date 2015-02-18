package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.net.URI;
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
    @Mock MetadataInfoEditingService metadataInfoEditingService;
    UriComponentsBuilder uriBuilder;
    Workflow workflow;
    CatalogueUser editor;
    private static final String FILENAME = "e5090602-6ff9-4936-8217-857ea6de5774";
    private static final URI metadataUrl = URI.create("/documents/" + FILENAME);
    private static final String PENDING_ID = "ykhm7b";
    private static final String DRAFT_ID = "qtak5r";
    private MetadataDocument draft, published;
    private GitPublicationService publicationService;
    
    @Before
    public void given() throws IOException {
        uriBuilder = UriComponentsBuilder.fromHttpUrl("https://example.com/documents").pathSegment(FILENAME, "publication");
        workflow = new PublicationConfig().workflow();
        editor = new CatalogueUser();
        editor.setUsername("Ron MacDonald");
        editor.setEmail("ron@example.com");
        
        this.draft = new GeminiDocument()
            .setTitle("draft")
            .setUri(URI.create("http://localhost"))
            .setMetadata(new MetadataInfo().setState("draft"));
        
        this.published = new GeminiDocument()
            .setTitle("published")
            .setUri(URI.create("http://localhost"))
            .setMetadata(new MetadataInfo().setState("published"));
        
        MockitoAnnotations.initMocks(this);
        
        this.publicationService = new GitPublicationService(groupStore, workflow, metadataInfoEditingService);
    }
    
    @Test
    public void successfullyTransitionState() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Arrays.asList(createGroup(DocumentController.EDITOR_ROLE)));
        when(metadataInfoEditingService.getMetadataDocument(any(String.class), any(URI.class))).thenReturn(draft);
        
        //When
        publicationService.transition(editor, FILENAME, PENDING_ID, uriBuilder, metadataUrl);
        
        //Then
        verify(metadataInfoEditingService).saveMetadataInfo(any(String.class), any(MetadataInfo.class), any(CatalogueUser.class), any(String.class));
    }
    
    @Test
    public void editorCannotTransitionFromPublic() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Arrays.asList(createGroup(DocumentController.EDITOR_ROLE)));
        when(metadataInfoEditingService.getMetadataDocument(any(String.class), any(URI.class))).thenReturn(published);
        
        //When
        publicationService.transition(editor, FILENAME, DRAFT_ID, uriBuilder, metadataUrl);
        
        //Then
        verify(metadataInfoEditingService, never()).saveMetadataInfo(any(String.class), any(MetadataInfo.class), any(CatalogueUser.class), any(String.class));
    }
    
    @Test
    public void publisherCanTransitionFromPublic() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Arrays.asList(createGroup(DocumentController.PUBLISHER_ROLE)));
        when(metadataInfoEditingService.getMetadataDocument(any(String.class), any(URI.class))).thenReturn(published);
        
        //When
        publicationService.transition(editor, FILENAME, DRAFT_ID, uriBuilder, metadataUrl);
        
        //Then
        verify(metadataInfoEditingService).saveMetadataInfo(any(String.class), any(MetadataInfo.class), any(CatalogueUser.class), any(String.class));
    }
    
    @Test
    public void unknownCannotTransitionFromPublic() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Collections.EMPTY_LIST);
        when(metadataInfoEditingService.getMetadataDocument(any(String.class), any(URI.class))).thenReturn(published);
        
        //When
        publicationService.transition(editor, FILENAME, DRAFT_ID, uriBuilder, metadataUrl);
        
        //Then
        verify(metadataInfoEditingService, never()).saveMetadataInfo(any(String.class), any(MetadataInfo.class), any(CatalogueUser.class), any(String.class));
    }
    
    @Test
    public void successfullyGetCurrentState() throws Exception {
        //Given
        when(metadataInfoEditingService.getMetadataDocument(any(String.class), any(URI.class))).thenReturn(draft);
        
        //When
        StateResource current = publicationService.current(editor, FILENAME, uriBuilder, metadataUrl);
        
        //Then
        assertThat("State is should be draft", current.getId(), equalTo("draft"));
    }
    
    @Test(expected = DocumentDoesNotExistException.class)
    public void tryToGetFileThatDoesNotExist() throws Exception {
        //Given 
        when(metadataInfoEditingService.getMetadataDocument(any(String.class), any(URI.class))).thenThrow(new DocumentDoesNotExistException("test"));
        
        //When
        publicationService.current(editor, "this file name does not exist", uriBuilder, metadataUrl);
        
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