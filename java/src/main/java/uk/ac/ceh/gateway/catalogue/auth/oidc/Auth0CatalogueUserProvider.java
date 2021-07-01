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
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import static uk.ac.ceh.gateway.catalogue.auth.oidc.DataLabsAuthenticationProvider.withAccessTokenAuthorization;

@Slf4j
@Profile("auth:datalabs")
@Service
@ToString(of = "userInfoEndpoint")
public class Auth0CatalogueUserProvider implements CatalogueUserProvider {
    private final RestTemplate restTemplate;
    private final String userInfoEndpoint;

    public Auth0CatalogueUserProvider(
        @Qualifier("normal") RestTemplate restTemplate,
        @Value("${datalabs.userInfo}") String userInfoEndpoint
    ) {
        this.restTemplate = restTemplate;
        this.userInfoEndpoint = userInfoEndpoint;
        log.info("Creating {}", this);
    }

    @Override
    public CatalogueUser provide(String token) {
        val userInfo = restTemplate.exchange(
            userInfoEndpoint,
            HttpMethod.GET,
            withAccessTokenAuthorization(token),
            UserInfo.class
        ).getBody();
        log.debug(userInfo.toString());
        val user = new CatalogueUser();
        user.setUsername(userInfo.getName());
        user.setEmail(userInfo.getName());
        return user;
    }

    @lombok.Value
    public static class UserInfo {
        String sub;
        String name;

        @JsonCreator
        public UserInfo(
            @JsonProperty("sub") String sub,
            @JsonProperty("name") String name
        ) {
            this.sub = sub;
            this.name = name;
        }
    }
}
