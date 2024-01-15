package uk.ac.ceh.gateway.catalogue.publication;

import lombok.Value;
import lombok.Builder;

@Value
@Builder
public class Transition {
    private final State toState;
    private final String id, title, helpText;
    
    public static Transition UNKNOWN_TRANSITION = Transition.builder()
                                        .toState(State.UNKNOWN_STATE)
                                        .id("fi7tap")
                                        .title("Unknown Transition")
                                        .helpText("Something is wrong, do NOT try publishing to this state")
                                        .build();
}
