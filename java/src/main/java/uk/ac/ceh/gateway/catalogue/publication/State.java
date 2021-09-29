package uk.ac.ceh.gateway.catalogue.publication;

import com.google.common.collect.ImmutableSet;
import lombok.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"id", "title"})
@ToString(onlyExplicitlyIncluded = true)
public class State {
    @ToString.Include
    private final String id, title;
    @Getter(AccessLevel.NONE)
    private final Map<PublishingRole, Set<Transition>> transitions = new HashMap<>();

    public static State UNKNOWN_STATE = new State("unknown", "Unknown");

    public Set<Transition> availableTransitions(Set<PublishingRole> roles) {
        final Set<Transition> toReturn = new HashSet<>();
        roles
            .stream()
            .filter(role -> (transitions.containsKey(role)))
            .forEach((role) -> {
                toReturn.addAll(transitions.get(role));
            });
        return ImmutableSet.copyOf(toReturn);
    }

    public Transition getTransition(Set<PublishingRole> roles, String transitionId) {
        return availableTransitions(roles)
            .stream()
            .filter(t -> t.getId().equalsIgnoreCase(transitionId))
            .findFirst()
            .orElse(Transition.UNKNOWN_TRANSITION);
    }

    public boolean canTransition(Set<PublishingRole> roles, Transition transition) {
        boolean toReturn = false;
        for (PublishingRole role : roles) {
            if (transitions.containsKey(role)) {
                Set<Transition> available = transitions.get(role);
                if(available.contains(transition)) {
                    toReturn = true;
                }
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
