package uk.ac.ceh.gateway.catalogue.publication;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;

public class StateTest {
    private final State draft;
    private final PublishingRole editor;
    
    public StateTest() {
        //Given
        draft = new State("draft", "Draft");
        editor = new PublishingRole("ROLE_EDITOR");
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