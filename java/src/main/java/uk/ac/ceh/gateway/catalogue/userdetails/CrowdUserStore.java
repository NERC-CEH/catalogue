package uk.ac.ceh.gateway.catalogue.userdetails;

import com.fasterxml.jackson.databind.JsonNode;
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
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.Optional;

import static java.lang.String.format;
import static uk.ac.ceh.gateway.catalogue.util.Headers.withBasicAuth;

@Profile("auth:crowd")
@Service
public class CrowdUserStore implements UserStore<CatalogueUser> {
    private final String address;
    private final String crowdUsername;
    private final String crowdPassword;
    private final RestTemplate restTemplate;

    public CrowdUserStore(
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
    @Cacheable(value = "crowd-user")
    public CatalogueUser getUser(String username) throws UnknownUserException {
        try {
            val uriTemplate = address + "/user?username={username}&expand=attributes";
            val response = restTemplate.exchange(
                uriTemplate,
                HttpMethod.GET,
                new HttpEntity<>(withBasicAuth(crowdUsername, crowdPassword)),
                JsonNode.class,
                username
            );
            val node = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new UserDetailsException(format("No body for %s %s", uriTemplate, username)));
            return new CatalogueUser(
                node.get("name").asText(),
                node.get("email").asText()
            );
        } catch (HttpClientErrorException.NotFound ex) {
            throw new UnknownUserException(username);
        }
    }

    @Override
    public boolean userExists(String username) {
        throw new NotImplementedException(format("Cannot check if %s exists", username));
    }

    @Override
    public CatalogueUser authenticate(String username, String password) {
        throw new NotImplementedException(format("Cannot authenticate %s", username));
    }
}
