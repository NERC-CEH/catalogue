package uk.ac.ceh.gateway.catalogue.auth.oidc;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.crowd.model.CrowdGroup;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Profile("auth:datalabs")
@Service
public class AuthenticationGroupStore<CatalogueUser extends User> implements GroupStore<CatalogueUser> {
    public AuthenticationGroupStore() {
        log.info("Creating AuthenticationGroupStore");
    }

    @SneakyThrows
    @Override
    public List<Group> getGroups(CatalogueUser user) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        val principal = authentication.getPrincipal();
        if(!user.equals(principal)){
            throw new Exception("User does not match the principal");
        }
        val authorities = authentication.getAuthorities();
        log.debug("Granted Authorities in group store: {}", authorities);
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
