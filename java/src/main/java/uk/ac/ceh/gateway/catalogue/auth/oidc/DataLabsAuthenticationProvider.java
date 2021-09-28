package uk.ac.ceh.gateway.catalogue.auth.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static uk.ac.ceh.gateway.catalogue.controllers.DocumentController.MAINTENANCE_ROLE;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
@Service
@Profile("auth:datalabs")
public class DataLabsAuthenticationProvider implements AuthenticationProvider {
    private final RestTemplate restTemplate;
    @ToString.Include
    private final URI address;

    public static final String ADMIN = "system:catalogue:admin";
    public static final String PUBLISHER = "system:catalogue:publish";
    public static final String EDITOR = "system:catalogue:edit";
    public static final String DATALABS_PUBLISHER = "ROLE_DATALABS_PUBLISHER";
    public static final String DATALABS_EDITOR = "ROLE_DATALABS_EDITOR";

    public DataLabsAuthenticationProvider(
        @Qualifier("normal") RestTemplate restTemplate,
        @Value("${datalabs.userPermissions}") String address
    ) {
        this.restTemplate = restTemplate;
        this.address = UriComponentsBuilder.fromHttpUrl(address).build().toUri();
        log.info("Creating {}", this);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (authentication == null) {
            return null;
        }

        val grantedAuthorities = retrievePermissions(
            authentication.getCredentials().toString()
        );

        val token = new PreAuthenticatedAuthenticationToken(
                authentication.getPrincipal(),
                authentication.getCredentials(),
                grantedAuthorities
        );

        log.debug("Granted Authorities: {}", token.getAuthorities());
        token.setAuthenticated(true);
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }

    private List<SimpleGrantedAuthority> retrievePermissions(String accessToken) {
        val response = restTemplate.exchange(
                this.address,
                HttpMethod.GET,
                withAccessTokenAuthorization(accessToken),
                JsonNode.class
        );
        log.debug("Datalabs user permissions: {}", response.getBody());
        val userPermissionsNode = Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new AuthenticationException("Cannot get response body"))
            .at("/data/userPermissions");
        if (userPermissionsNode.isArray()) {
            return StreamSupport.stream(userPermissionsNode.spliterator(), false)
                .map(JsonNode::asText)
                .map(this::mapDataLabsPermissionsToCatalogueRoles)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public static HttpEntity<Object> withAccessTokenAuthorization(String accessToken) {
        val headers = new HttpHeaders();
        headers.add("authorization", format("bearer %s", accessToken));
        return new HttpEntity<>(headers);
    }

    private Optional<SimpleGrantedAuthority> mapDataLabsPermissionsToCatalogueRoles(String dataLabsPermission) {
        return switch (dataLabsPermission) {
            case ADMIN -> Optional.of(new SimpleGrantedAuthority(MAINTENANCE_ROLE));
            case PUBLISHER -> Optional.of(new SimpleGrantedAuthority(DATALABS_PUBLISHER));
            case EDITOR -> Optional.of(new SimpleGrantedAuthority(DATALABS_EDITOR));
            default -> Optional.empty();
        };
    }
}
