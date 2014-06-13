package uk.ac.ceh.gateway.catalogue.publication;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(of = {"id", "title"})
public class State {
    private final String id, title;
    @Getter(AccessLevel.NONE)
    private final Map<PublishingRole, Set<Transition>> transitions = new HashMap<>();
    
    public static State UNKNOWN_STATE = new State("unknown", "Unknown");
    
    public Set<Transition> avaliableTransitions(PublishingRole role) {
        Set<Transition> toReturn = Collections.EMPTY_SET;
        if (transitions.containsKey(role)) {
            toReturn = ImmutableSet.copyOf(transitions.get(role));
        }
        return toReturn;
    }
    
    public boolean canTransition(PublishingRole role, Transition transition) {
        boolean toReturn = false;
        if (transitions.containsKey(role)) {
            Set<Transition> available = transitions.get(role);
            if(available.contains(transition)) {
                toReturn = true;
            }
        }
        return toReturn;
    }
    
    public void addTransitions(PublishingRole role, Set<Transition> transitions) {
        for (Transition transition : transitions) {
            if (transition.getToState().equals(this)) {
                throw new PublicationException(String.format("Cannot transition State: %s to itself", this.title));
            }            
        }
        this.transitions.put(role, transitions);
    }
}