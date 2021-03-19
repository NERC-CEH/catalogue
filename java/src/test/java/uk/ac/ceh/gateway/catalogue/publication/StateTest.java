package uk.ac.ceh.gateway.catalogue.publication;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class StateTest {
    private final State draft;
    private final PublishingRole editor;
    
    public StateTest() {
        //Given
        draft = new State("draft", "Draft");
        editor = new PublishingRole("ROLE_EDITOR");
    }
    
    @Test
    public void cannotAddSelfToTransition() {
        Assertions.assertThrows(PublicationException.class, () -> {
            //Given
            Transition toSelf = Transition.builder().title("To Self").toState(draft).build();

            //When
            draft.addTransitions(editor, ImmutableSet.of(toSelf));
        });
    }
    
    @Test
    public void unknownRoleHasNoTransitions() {
        //When
        final Set<Transition> avaliableTransitions = draft.avaliableTransitions(ImmutableSet.of(new PublishingRole("ROLE_UNKNOWN")));
        
        //Then
        assertThat("Should be no available Transitions", avaliableTransitions.size(), equalTo(0));
    }
    
}