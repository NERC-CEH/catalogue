package uk.ac.ceh.gateway.catalogue.publication;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import static org.hamcrest.CoreMatchers.*;
import uk.ac.ceh.gateway.catalogue.config.PublicationConfig;
import uk.ac.ceh.gateway.catalogue.controllers.DocumentController;

public class WorkflowTest {
    private final PublishingRole editor, publisher;
    private final Workflow workflow;

    public WorkflowTest() {
        //Given
        workflow = new PublicationConfig().workflow();
        editor = new PublishingRole(DocumentController.EDITOR_ROLE);
        publisher = new PublishingRole(DocumentController.PUBLISHER_ROLE);
    }
    
    @Test
    public void getCurrentStateOfDocumentInDraft() {
        //Given
        MetadataInfo info = new MetadataInfo().setState("draft");
        
        //When
        final State currentState = workflow.currentState(info);
        
        //Then
        assertThat("state should be draft", currentState.getId(), equalTo("draft"));
    }
    
    @Test
    public void editorTransitionDraftToPending() {
        //Given
        MetadataInfo original = new MetadataInfo().setState("draft");
        
        final State currentState = workflow.currentState(original);
        final Transition toPending = getTransitionTo(currentState.avaliableTransitions(ImmutableSet.of(editor)), "pending");
        
        //When
        MetadataInfo transitioned = workflow.transitionDocumentState(original, ImmutableSet.of(editor), toPending);
        
        //Then
        assertThat("state should be pending", transitioned.getState(), equalTo("pending"));
        assertThat("transitioned should not be equal to original", transitioned, not(equalTo(original)));
    }
    
    @Test
    public void editorCannotTransitionPendingToPublished() {
        //Given
        MetadataInfo info = new MetadataInfo().setState("pending");
        
        final State currentState = workflow.currentState(info);
        final Transition toPublished = getTransitionTo(currentState.avaliableTransitions(ImmutableSet.of(editor)), "published");
        
        //When
        info = workflow.transitionDocumentState(info, ImmutableSet.of(editor), toPublished);
        
        //Then
        assertThat("state should be pending", info.getState(), equalTo("pending"));
    }
    
    @Test
    public void publisherCanTransitionPendingToPublished() {
        //Given
        MetadataInfo info = new MetadataInfo().setState("pending");
        
        final State currentState = workflow.currentState(info);
        final Transition toPublished = getTransitionTo(currentState.avaliableTransitions(ImmutableSet.of(publisher)), "published");
        
        //When
        info = workflow.transitionDocumentState(info, ImmutableSet.of(editor, publisher), toPublished);
        
        //Then
        assertThat("state should be published", info.getState(), equalTo("published"));
    }
    
    @Test
    public void editorCanNotTransitionFromPublishedToDraft() {
        //Given
        MetadataInfo info = new MetadataInfo().setState("published");
        
        final State currentState = workflow.currentState(info);
        final Transition toDraft = getTransitionTo(currentState.avaliableTransitions(ImmutableSet.of(editor)), "draft");
        
        //When
        info = workflow.transitionDocumentState(info, ImmutableSet.of(editor), toDraft);
        
        //Then
        assertThat("state should be published", info.getState(), equalTo("published"));   
    }
    
    private Transition getTransitionTo(Set<Transition> transitions, String state) {
        Transition toReturn = Transition.UNKNOWN_TRANSITION;
        for (Transition transition : transitions) {
            if (transition.getToState().getId().equalsIgnoreCase(state)) {
                toReturn = transition;
            }   
        }
        return toReturn;
    }
}
