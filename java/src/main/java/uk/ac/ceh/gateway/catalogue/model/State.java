package uk.ac.ceh.gateway.catalogue.model;

import java.util.Set;
import lombok.Value;
import lombok.experimental.Builder;

@Value
@Builder
public class State {
    String id, title;
    Set<Transition> transitions;
    
    @Value
    public static class Transition {
        String id, title, helpText, href;
        Set<String> roles;
    }
}