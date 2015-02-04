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
    public void editorCannotTransitionPendingToPublic() {
        //Given
        MetadataInfo info = new MetadataInfo().setState("pending");
        
        final State currentState = workflow.currentState(info);
        final Transition toPublic = getTransitionTo(currentState.avaliableTransitions(ImmutableSet.of(editor)), "public");
        
        //When
        info = workflow.transitionDocumentState(info, ImmutableSet.of(editor), toPublic);
        
        //Then
        assertThat("state should be pending", info.getState(), equalTo("pending"));
    }
    
    @Test
    public void publisherCanTransitionPendingToPublic() {
        //Given
        MetadataInfo info = new MetadataInfo().setState("pending");
        
        final State currentState = workflow.currentState(info);
        final Transition toPublic = getTransitionTo(currentState.avaliableTransitions(ImmutableSet.of(publisher)), "public");
        
        //When
        info = workflow.transitionDocumentState(info, ImmutableSet.of(editor, publisher), toPublic);
        
        //Then
        assertThat("state should be public", info.getState(), equalTo("public"));
    }
    
    @Test
    public void editorCanNotTransitionFromPublicToDraft() {
        //Given
        MetadataInfo info = new MetadataInfo().setState("public");
        
        final State currentState = workflow.currentState(info);
        final Transition toDraft = getTransitionTo(currentState.avaliableTransitions(ImmutableSet.of(editor)), "draft");
        
        //When
        info = workflow.transitionDocumentState(info, ImmutableSet.of(editor), toDraft);
        
        //Then
        assertThat("Tate should be public", info.getState(), equalTo("public"));   
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
