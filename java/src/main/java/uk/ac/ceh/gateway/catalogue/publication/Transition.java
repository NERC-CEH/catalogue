package uk.ac.ceh.gateway.catalogue.publication;

import lombok.Value;
import lombok.experimental.Builder;

@Value
@Builder
public class Transition {
    private final State toState;
    private final String title, helpText, confirmationQuestion;
}