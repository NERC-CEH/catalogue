package uk.ac.ceh.gateway.catalogue.publication;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;

public class StateTest {
    private final State draft, pending, publik;
    private final PublishingRole editor, publisher;
    
    public StateTest() {
        //Given
        draft = new State("draft", "Draft");
        pending = new State("pending", "Pending");
        publik = new State("public", "Public");
        
        Transition draftToPending = Transition.builder()
            .toState(pending)
            .title("Request publication of document")
            .helpText("Requesting publication of this document will notify a Publisher that you want this document to be made public. A Publisher will review this record for quality against CEH QA 2.4c, 3.45 and 76.4a rev9.")
            .confirmationQuestion("Submit \"{title (first 100 characters)}\" for publication?")
            .build();
        
        editor = new PublishingRole("ROLE_EDITOR");
        publisher = new PublishingRole("ROLE_PUBLISHER");
        
        draft.addTransitions(editor, ImmutableSet.of(draftToPending));
        draft.addTransitions(publisher, ImmutableSet.of(draftTo));
        
        pending.addTransitions(editor, ImmutableSet.of(draft));
        pending.addTransitions(publisher, ImmutableSet.of(draft, publik));
        
        publik.addTransitions(publisher, ImmutableSet.of(draft));
    }
    
    @Test
    public void transitionsAvailableForPublisherFromDraft() {
        //When
        final Set<Transition> actual = draft.avaliableTransitions(publisher);
        
        //Then
        assertThat("should only be one state", actual.size(), equalTo(1));
        
    }
    
    @Test
    public void transitionsAvailableForPublisherFromPending() {
        //When
        final Set<State> actual = pending.avaliableTransitions(publisher);
        
        //Then
        assertThat("actual set of states should contain only 'draft' and 'public'", actual, hasItems(draft, publik));
        assertThat("should only be one state", actual.size(), equalTo(2));
        
    }
    
    @Test
    public void publisherCanTransitionFromDraftToPending() {
        //When
        final boolean canTransition = draft.canTransition(publisher, pending);
        
        //Then
        assertThat("Publisher should be able to transition from draft to pending", canTransition, is(true));   
    }
    
    @Test
    public void editorCanTransitionFromDraftToPending() {
        //When
        final boolean canTransition = draft.canTransition(editor, pending);
        
        //Then
        assertThat("Editor should be able to transition from draft to pending", canTransition, is(true));   
    }
    
    @Test
    public void editorCanNotTransitionFromPendingToPublic() {
        //When
        final boolean canTransition = pending.canTransition(editor, publik);
        
        //Then
        assertThat("Editor should not be able to transition from pending to public", canTransition, is(false));   
    }
    
    @Test
    public void editorCanNotTransitionFromPublic() {
        //When
        final boolean canTransition = publik.canTransition(editor, draft);
        
        //Then
        assertThat("Editor should not be able to transition from public to draft", canTransition, is(false));   
    }
    
    @Test
    public void editorHasNoTransitionsFromPublic() {
        //When
        final Set<State> actual = publik.avaliableTransitions(editor);
        
        //Then
        assertThat("Editor should have no Transitions from public", actual.size(), equalTo(0));   
    }
    
    @Test(expected = PublicationException.class)
    public void cannotAddSelfToTransition() {
        //Given
        Transition toSelf = Transition.builder().title("To Self").toState(draft).build();
        
        //When
        draft.addTransitions(editor, ImmutableSet.of(toSelf));
    }
    
    @Test
    public void unknownRoleHasNoTransitions() {
        //When
        final Set<Transition> avaliableTransitions = draft.avaliableTransitions(new PublishingRole("ROLE_UNKNOWN"));
        
        //Then
        assertThat("Should be no available Transitions", avaliableTransitions.size(), equalTo(0));
    }
    
}