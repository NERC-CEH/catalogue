package uk.ac.ceh.gateway.catalogue.publication;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.config.PublicationConfig;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.PublicationServiceException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GitPublicationServiceTest {
    @Mock GroupStore<CatalogueUser> groupStore;
    @Mock DocumentRepository documentRepository;
    Workflow workflow;
    CatalogueUser editor;
    private static final String FILENAME = "e5090602-6ff9-4936-8217-857ea6de5774";
    private static final String PENDING_ID = "ykhm7b";
    private static final String DRAFT_ID = "qtak5r";
    private MetadataDocument draft, published;
    private GitPublicationService publicationService;

    @BeforeEach
    public void given() throws IOException {
        workflow = new PublicationConfig().workflow();
        editor = new CatalogueUser( "Ron MacDonald", "ron@example.com");

        this.draft = new GeminiDocument()
            .setTitle("draft")
            .setId("3beb9650-fc88-4de5-b8cd-9cc8a4abe135")
            .setMetadata(MetadataInfo.builder().state("draft").catalogue("eidc").build());

        this.published = new GeminiDocument()
            .setTitle("published")
            .setId("db49a6ee-5c9e-4bef-8e6e-196387df4d97")
            .setMetadata(MetadataInfo.builder().state("published").catalogue("eidc").build());

        this.publicationService = new GitPublicationService(groupStore, workflow, documentRepository);
    }

    @Test
    public void successfullyTransitionState() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Collections.singletonList(createGroup("ROLE_EIDC_EDITOR")));
        when(documentRepository.read(FILENAME)).thenReturn(draft);

        //When
        publicationService.transition(editor, FILENAME, PENDING_ID);

        //Then
        verify(documentRepository).read(FILENAME);
        verify(documentRepository).save(editor, draft, FILENAME, "Publication state of e5090602-6ff9-4936-8217-857ea6de5774 changed.");
    }

    @Test
    public void editorCannotTransitionFromPublic() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Collections.singletonList(createGroup("ROLE_EIDC_EDITOR")));
        when(documentRepository.read(FILENAME)).thenReturn(published);

        //When
        StateResource actual = publicationService.transition(editor, FILENAME, DRAFT_ID);

        //Then
        verify(documentRepository).read(FILENAME);
        verify(documentRepository).save(editor, published, FILENAME, "Publication state of e5090602-6ff9-4936-8217-857ea6de5774 changed.");
        assertThat("State should still be published", actual.getTitle(), equalTo("Published"));
    }

    @Test
    public void publisherCanTransitionFromPublic() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Collections.singletonList(createGroup("ROLE_EIDC_PUBLISHER")));
        when(documentRepository.read(FILENAME)).thenReturn(published);

        //When
        StateResource actual = publicationService.transition(editor, FILENAME, DRAFT_ID);

        //Then
        verify(documentRepository).read(FILENAME);
        verify(documentRepository).save(editor, published, FILENAME, "Publication state of e5090602-6ff9-4936-8217-857ea6de5774 changed.");
        assertThat("State should be draft", actual.getTitle(), equalTo("Draft"));
    }

    @Test
    public void unknownCannotTransitionFromPublic() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Collections.emptyList());
        when(documentRepository.read(FILENAME)).thenReturn(published);

        //When
        StateResource actual = publicationService.transition(editor, FILENAME, DRAFT_ID);

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
        StateResource current = publicationService.current(editor, FILENAME);

        //Then
        verify(documentRepository).read(FILENAME);
        assertThat("State is should be draft", current.getId(), equalTo("draft"));
    }

    @Test
    public void tryToGetFileThatDoesNotExist() {
        val fileDoesNotExist = "this file name does not exist";
        Assertions.assertThrows(PublicationServiceException.class, () -> {
            //Given
            when(documentRepository.read(fileDoesNotExist)).thenThrow(new PublicationServiceException("test"));

            //When
            publicationService.current(editor, fileDoesNotExist);

            //Then
            verify(documentRepository).read(FILENAME);
        });
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
