package uk.ac.ceh.gateway.catalogue.auth.oidc;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.crowd.model.CrowdGroup;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Profile("auth:datalabs")
@Service
public class DataLabsGroupStore<CatalogueUser extends User> implements GroupStore<CatalogueUser> {

    @SneakyThrows
    @Override
    public List<Group> getGroups(CatalogueUser user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CatalogueUser principalUser = (CatalogueUser) authentication.getPrincipal();
        if(user.getUsername() != principalUser.getUsername()){
            throw new Exception("Username does not match the principle");
        }
        Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) authentication.getAuthorities();
        return authorities
                .stream()
                .map(authority -> new CrowdGroup(authority.getAuthority().toUpperCase(), ""))
                .collect(Collectors.toList());
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
