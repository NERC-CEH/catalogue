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
    State draft, pending, publik;

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
        assertThat("state should be draft", currentState, equalTo(draft));
    }
    
    @Test
    public void editorTransitionDraftToPending() {
        //Given
        GeminiDocument document = new GeminiDocument()
            .setId("c4d0393e-ff0a-47da-84e0-09ca9182e6cb")
            .setTitle("MultiMOVE: A Package of niche models for British vegetation")
            .setMetadata(new MetadataInfo("test", "", "draft"));
        
        //When
        final State currentState = workflow.currentState(document);
        final Set<Transition> avaliableTransitions = currentState.avaliableTransitions(editor);
        final Transition transition = avaliableTransitions.iterator().next();
        document = workflow.transitionDocumentState(document, editor, transition);
        
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
        
        //When
        final State currentState = workflow.currentState(document);
        document = workflow.transitionDocumentState(document, editor, );
        
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
        
        //When
        final State currentState = workflow.currentState(document);
        document = workflow.transitionDocumentState(document, publisher, publik);
        
        //Then
        assertThat("state should be public", document.getMetadata().getState(), equalTo("public"));
    }
    
    
}
