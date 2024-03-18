package uk.ac.ceh.gateway.catalogue.auth.oidc;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.Optional;
import java.util.stream.StreamSupport;

import static uk.ac.ceh.gateway.catalogue.auth.oidc.DataLabsAuthenticationProvider.withAccessTokenAuthorization;

@Slf4j
@Profile("auth:datalabs")
@Service
@ToString(onlyExplicitlyIncluded = true)
public class DatalabsCatalogueUserProvider implements CatalogueUserProvider {
    private final RestTemplate restTemplate;
    @ToString.Include
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
        val response = restTemplate.exchange(
            usersEndpoint,
            HttpMethod.GET,
            withAccessTokenAuthorization(token),
            JsonNode.class
        );
        log.debug(response.toString());
        val usersNode = Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new AuthenticationException("Cannot get response body for: " + subject))
            .at("/data/users");

        if (usersNode.isArray()) {
            val name = StreamSupport.stream(usersNode.spliterator(), false)
                .filter(node -> node.get("userId").asText().equals(subject))
                .findFirst()
                .orElseThrow(() -> new AuthenticationException("No user found for " + subject))
                .get("name")
                .asText();
            return new CatalogueUser(name, name);
        } else {
            throw new AuthenticationException("No Users array present for " + subject);
        }
    }
}
