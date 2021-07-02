package uk.ac.ceh.gateway.catalogue.auth.oidc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.ac.ceh.gateway.catalogue.auth.oidc.DataLabsAuthenticationProvider.withAccessTokenAuthorization;

@Slf4j
@Profile("auth:datalabs")
@Service
@ToString(of = "usersEndpoint")
public class DatalabsCatalogueUserProvider implements CatalogueUserProvider {
    private final RestTemplate restTemplate;
    private final URI usersEndpoint;

    public DatalabsCatalogueUserProvider(
        @Qualifier("normal") RestTemplate restTemplate,
        @Value("${datalabs.users}") String usersEndpoint
    ) {
        this.restTemplate = restTemplate;
        this.usersEndpoint = UriComponentsBuilder.fromHttpUrl(usersEndpoint).build().toUri();
        log.info("Creating {}", this);
    }

    @Override
    public CatalogueUser provide(String subject, String token) {
        val users = restTemplate.exchange(
            usersEndpoint,
            HttpMethod.GET,
            withAccessTokenAuthorization(token),
            Users.class
        ).getBody();
        log.debug(users.toString());
        val possibleUsers = Optional.ofNullable(users.getData().getUsers());
        return possibleUsers.orElse(Collections.emptyList())
            .stream()
            .filter(user -> user.getUserId().equals(subject))
            .map(user ->
                new CatalogueUser().setUsername(user.getName()).setEmail(user.getName())
            ).findFirst()
            .orElseThrow(
                () -> new AuthenticationException("No user found for subject: " + subject)
            );
    }

    @lombok.Value
    public static class Users {
        Data data;

        @JsonCreator
        public Users(
            @JsonProperty("data") Data data
        ) {
            this.data = data;
        }

        @lombok.Value
        public static class Data {
            List<User> users;

            @JsonCreator
            public Data(
                @JsonProperty("users") List<User> users
            ) {
                this.users = users;
            }

            @lombok.Value
            public static class User {
                String userId;
                String name;

                @JsonCreator
                public User(
                    @JsonProperty("userId") String userId,
                    @JsonProperty("name") String name
                ) {
                    this.userId = userId;
                    this.name = name;
                }
            }
        }
    }
}
