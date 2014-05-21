package uk.ac.ceh.gateway.catalogue.model;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import uk.ac.ceh.components.userstore.Group;

/**
 *
 * @author Christopher Johnson
 */
public class Groups {
    private final Collection<Group> roles, organisations;
    
    public Groups(List<Group> groups) {
        Predicate<Group> isRolePredicate = g->g.getName().startsWith("ROLE_");
        this.roles = groups.stream()
                           .filter(isRolePredicate)
                           .collect(Collectors.toList());
        this.organisations = groups.stream()
                                   .filter(isRolePredicate.negate())
                                   .collect(Collectors.toList());
    }

    public Collection<Group> getRoles() {
        return roles;
    }

    public Collection<Group> getOrganisations() {
        return organisations;
    }
    
    public Collection<String> getOrganisationNames() {
        return getOrganisations().stream()
                                 .map(g->g.getName())
                                 .collect(Collectors.toList());
    }

    public Collection<String> getRoleNames() {
        return getRoles().stream()
                         .map(g->g.getName())
                         .collect(Collectors.toList());
    }
}