package uk.ac.ceh.gateway.catalogue.publication;

import lombok.Value;
import lombok.experimental.Builder;

@Value
@Builder
public class Transition {
    private final State toState;
    private final String title, helpText, confirmationQuestion;
    
    public static Transition UNKNOWN_TRANSITION = Transition.builder()
                                        .title("Unknown Transition")
                                        .helpText("Something is wrong, do NOT try publishing to this state")
                                        .confirmationQuestion("Do you really want to publish to this broken state?")
                                        .toState(State.UNKNOWN_STATE)
                                        .build();
}