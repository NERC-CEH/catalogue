package uk.ac.ceh.gateway.catalogue.publication;

import lombok.Value;

@Value
public class TransitionResource {
    private final String id, title, helpText, cssClass;

    public TransitionResource (State fromState, Transition transition) {
        this.id = transition.getId();
        this.title = transition.getTitle();
        this.helpText = transition.getHelpText();
        this.cssClass = transition.getCssClass();
    }
}
