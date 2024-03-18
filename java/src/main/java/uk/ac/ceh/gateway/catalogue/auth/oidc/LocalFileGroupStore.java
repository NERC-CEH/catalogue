package uk.ac.ceh.gateway.catalogue.auth.oidc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.components.userstore.crowd.model.CrowdGroup;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Profile("auth:oidc")
@Service
public class LocalFileGroupStore<CatalogueUser extends User> implements GroupStore<CatalogueUser> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TypeReference<Map<String, List<String>>> typeRef = new TypeReference<>() {};
    private final String rolesPath;

    public LocalFileGroupStore(@Value("${auth.oidc.roles.location}") String rolesPath) {
        log.info("Creating LocalFileGroupStore");
        this.rolesPath = rolesPath;
    }

    /**
     * Retrieves the groups associated with the specified user from a local file.
     *
     * @param user The CatalogueUser to look up the groups of
     * @return A list of Group objects representing the user's roles
     */
    @SneakyThrows
    @Override
    public List<Group> getGroups(CatalogueUser user) {
        log.info("Get groups for user {}", user);
        if (user != null) {
            val userRoles = objectMapper
                .readValue(new File(rolesPath), typeRef)
                .get(user.getUsername());
            if (userRoles != null) {
                return userRoles
                        .stream()
                        .map(role -> new CrowdGroup(role.toUpperCase(), ""))
                        .collect(Collectors.toList());
            }

        }
        return Collections.emptyList();
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
