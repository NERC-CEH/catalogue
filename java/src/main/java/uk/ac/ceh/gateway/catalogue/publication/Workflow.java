package uk.ac.ceh.gateway.catalogue.publication;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;

@Value
public class Workflow {
    @Getter(AccessLevel.NONE)
    private final Map<String, State> states;
    
    public State currentState(GeminiDocument document) {
        State currentState = State.UNKNOWN_STATE;
        String state = getMetadataInfo(document).getState().toLowerCase();
        
        if (states.containsKey(state)) {
            currentState = states.get(state);
        }
        return currentState;
    }
    
    public GeminiDocument transitionDocumentState(GeminiDocument document, PublishingRole role, Transition transition) {
        State fromState;
        fromState = currentState(document);
        
        if (fromState.canTransition(role, transition)) {
            getMetadataInfo(document).setState(transition.getToState().getId());
        }
        return document;
    }
    
    private MetadataInfo getMetadataInfo(GeminiDocument document) {
        MetadataInfo metadataInfo;
        try {
            metadataInfo = checkNotNull(document.getMetadata());
        } catch (NullPointerException ex) {
            throw new PublicationException(String.format("Could not retrieve metadata info for document: %s", document.getId()), ex);
        }
        return metadataInfo;
    }
}