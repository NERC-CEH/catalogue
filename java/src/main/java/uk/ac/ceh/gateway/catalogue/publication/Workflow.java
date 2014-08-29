package uk.ac.ceh.gateway.catalogue.publication;

import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;

@Value
public class Workflow {
    @Getter(AccessLevel.NONE)
    private final Map<String, State> states;
    
    public State currentState(MetadataInfo info) {
        State currentState = State.UNKNOWN_STATE;
        String state = info.getState().toLowerCase();
        
        if (states.containsKey(state)) {
            currentState = states.get(state);
        }
        return currentState;
    }
    
    public MetadataInfo transitionDocumentState(MetadataInfo info, Set<PublishingRole> roles, Transition transition) {
        MetadataInfo toReturn = info;
        State fromState = currentState(info);
        
        if (fromState.canTransition(roles, transition)) {
            toReturn = new MetadataInfo(info.getRawType(), transition.getToState().getId(), info.getDocumentType());
        }
        return toReturn;
    }
}