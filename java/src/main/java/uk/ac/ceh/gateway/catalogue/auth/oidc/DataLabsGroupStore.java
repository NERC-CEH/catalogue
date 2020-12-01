package uk.ac.ceh.gateway.catalogue.auth.oidc;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.WritableGroupStore;
import uk.ac.ceh.components.userstore.crowd.model.CrowdGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DataLabsGroupStore<U extends User> implements WritableGroupStore<U> {
    @Override
    public Group createGroup(String groupname, String description) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Group updateGroup(String groupName, String description) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteGroup(String groupname) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean grantGroupToUser(U user, String groupname) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean revokeGroupFromUser(U user, String groupname) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Group> getGroups(U user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) authentication.getAuthorities();
        ArrayList<Group> groups = new ArrayList<>();
        for(GrantedAuthority authority: authorities){
            Group group = new CrowdGroup(authority.getAuthority().toUpperCase(), "");
            groups.add(group);
        }
        return groups;
    }

    @Override
    public Group getGroup(String name) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Group> getAllGroups() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isGroupInExistance(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isGroupDeletable(String group) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }
}
