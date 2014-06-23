package uk.ac.ceh.gateway.catalogue.publication;

import java.util.HashSet;
import java.util.Set;
import lombok.Value;
import org.springframework.web.util.UriComponentsBuilder;

@Value
public class StateResource {
    private final String id, title;
    private final Set<TransitionResource> transitions;

    public StateResource(State currentState, Set<PublishingRole> roles, UriComponentsBuilder builder) {
        this.id = currentState.getId();
        this.title = currentState.getTitle();
        this.transitions = new HashSet<>();
        currentState.avaliableTransitions(roles).stream().forEach((transition) -> {
            transitions.add(new TransitionResource(currentState, transition, builder));
        });
    } 
}