package uk.ac.ceh.gateway.catalogue.userdetails;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.gateway.catalogue.model.CatalogueGroup;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static uk.ac.ceh.gateway.catalogue.util.Headers.withBasicAuth;

@Profile("auth:crowd")
@Service
public class CrowdGroupStore implements GroupStore<CatalogueUser> {
    private final String address;
    private final String crowdUsername;
    private final String crowdPassword;
    private final RestTemplate restTemplate;

    public CrowdGroupStore(
        @Value("${crowd.address}") String address,
        @Value("${crowd.username}") String crowdUsername,
        @Value("${crowd.password}") String crowdPassword,
        @Qualifier("normal") RestTemplate restTemplate
    ) {
        this.address = address;
        this.crowdUsername = crowdUsername;
        this.crowdPassword = crowdPassword;
        this.restTemplate = restTemplate;
    }

    @Override
    @SneakyThrows
    @Cacheable(value="crowd-user-groups", key="#user.username")
    public List<Group> getGroups(CatalogueUser user) {
        try {
            val uriTemplate = address + "/user/group/nested?username={username}";
            val response = restTemplate.exchange(
                uriTemplate,
                HttpMethod.GET,
                new HttpEntity<>(withBasicAuth(crowdUsername, crowdPassword)),
                JsonNode.class,
                user.getUsername()
            );
            val node = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new UserDetailsException(format("No body for %s %s", uriTemplate, user.getUsername())))
                .at("/groups");
            if (node.isArray()) {
                return StreamSupport.stream(node.spliterator(), false)
                    .map(groupNode -> {
                        val name = groupNode.get("name").asText();
                        return new CatalogueGroup(name);
                    })
                    .collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        } catch (HttpClientErrorException.NotFound ex) {
            throw new UnknownUserException(user.getUsername());
        }
    }

    @Override
    public Group getGroup(String name) throws IllegalArgumentException {
        throw new NotImplementedException(format("Cannot get group %s", name));
    }

    @Override
    public List<Group> getAllGroups() {
        throw new NotImplementedException("cannot get all groups");
    }

    @Override
    public boolean isGroupInExistance(String name) {
        throw new NotImplementedException(format("Cannot check for existence of group %s", name));
    }

    @Override
    public boolean isGroupDeletable(String group) throws IllegalArgumentException {
        throw new NotImplementedException(format("Cannot check if deletable group %s", group));
    }
}
