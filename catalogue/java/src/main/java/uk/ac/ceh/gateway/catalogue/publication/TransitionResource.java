package uk.ac.ceh.gateway.catalogue.publication;

import lombok.Value;
import org.springframework.web.util.UriComponentsBuilder;

@Value
public class TransitionResource {
    private final String id, title, helpText, href;
    
    public TransitionResource (State fromState, Transition transition, UriComponentsBuilder builder) {
        this.id = transition.getId();
        this.title = transition.getTitle();
        this.helpText = transition.getHelpText();
        this.href = builder.buildAndExpand(transition.getId()).toUriString();
    }
}