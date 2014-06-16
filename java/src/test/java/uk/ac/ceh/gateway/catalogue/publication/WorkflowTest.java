package uk.ac.ceh.gateway.catalogue.publication;

import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import static org.hamcrest.CoreMatchers.*;
import uk.ac.ceh.gateway.catalogue.config.PublicationConfig;

public class WorkflowTest {
    private final PublishingRole editor, publisher;
    private final Workflow workflow;

    public WorkflowTest() {
        //Given
        workflow = new PublicationConfig().workflow();
        editor = new PublishingRole("ROLE_EDITOR");
        publisher = new PublishingRole("ROLE_PUBLISHER");
    }
    
    @Test(expected = PublicationException.class)
    public void getCurrentStateOfDocumentWithNoMetadataInfo() {
        //Given
        GeminiDocument noMetadataInfo = new GeminiDocument()
            .setId("c4d0393e-ff0a-47da-84e0-09ca9182e6cb")
            .setTitle("MultiMOVE: A Package of niche models for British vegetation");
        
        //When
        workflow.currentState(noMetadataInfo);
        
        //Then
        // Expect PublicationException
    }
    
    @Test
    public void getCurrentStateOfDocumentInDraft() {
        //Given
        GeminiDocument document = new GeminiDocument()
            .setId("c4d0393e-ff0a-47da-84e0-09ca9182e6cb")
            .setTitle("MultiMOVE: A Package of niche models for British vegetation")
            .setMetadata(new MetadataInfo("test", "", "draft"));
        
        //When
        final State currentState = workflow.currentState(document);
        
        //Then
        assertThat("state should be draft", currentState.getId(), equalTo("draft"));
    }
    
    @Test
    public void editorTransitionDraftToPending() {
        //Given
        GeminiDocument document = new GeminiDocument()
            .setId("c4d0393e-ff0a-47da-84e0-09ca9182e6cb")
            .setTitle("MultiMOVE: A Package of niche models for British vegetation")
            .setMetadata(new MetadataInfo("test", "", "draft"));
        
        final State currentState = workflow.currentState(document);
        final Transition toPending = getTransitionTo(currentState.avaliableTransitions(editor), "pending");
        
        //When
        document = workflow.transitionDocumentState(document, editor, toPending);
        
        //Then
        assertThat("state should be pending", document.getMetadata().getState(), equalTo("pending"));
    }
    
    @Test
    public void editorCannotTransitionPendingToPublic() {
        //Given
        GeminiDocument document = new GeminiDocument()
            .setId("c4d0393e-ff0a-47da-84e0-09ca9182e6cb")
            .setTitle("MultiMOVE: A Package of niche models for British vegetation")
            .setMetadata(new MetadataInfo("test", "", "pending"));
        
        final State currentState = workflow.currentState(document);
        final Transition toPublic = getTransitionTo(currentState.avaliableTransitions(editor), "public");
        
        //When
        document = workflow.transitionDocumentState(document, editor, toPublic);
        
        //Then
        assertThat("state should be pending", document.getMetadata().getState(), equalTo("pending"));
    }
    
    @Test
    public void publisherCanTransitionPendingToPublic() {
        //Given
        GeminiDocument document = new GeminiDocument()
            .setId("c4d0393e-ff0a-47da-84e0-09ca9182e6cb")
            .setTitle("MultiMOVE: A Package of niche models for British vegetation")
            .setMetadata(new MetadataInfo("test", "", "pending"));
        
        final State currentState = workflow.currentState(document);
        final Transition toPublic = getTransitionTo(currentState.avaliableTransitions(publisher), "public");
        
        //When
        document = workflow.transitionDocumentState(document, publisher, toPublic);
        
        //Then
        assertThat("state should be public", document.getMetadata().getState(), equalTo("public"));
    }
    
    @Test
    public void editorCanNotTransitionFromPublicToDraft() {
        //Given
        GeminiDocument document = new GeminiDocument()
            .setId("c4d0393e-ff0a-47da-84e0-09ca9182e6cb")
            .setTitle("MultiMOVE: A Package of niche models for British vegetation")
            .setMetadata(new MetadataInfo("test", "", "public"));
        
        final State currentState = workflow.currentState(document);
        final Transition toDraft = getTransitionTo(currentState.avaliableTransitions(editor), "draft");
        
        //When
        document = workflow.transitionDocumentState(document, editor, toDraft);
        
        //Then
        assertThat("Tate should be public", document.getMetadata().getState(), equalTo("public"));   
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
