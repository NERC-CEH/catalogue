package uk.ac.ceh.gateway.catalogue.auth.oidc;

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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@ToString(of = "address")
@Service
@Profile("auth:datalabs")
public class DataLabsAuthenticationProvider implements AuthenticationProvider {

    private final RestTemplate restTemplate;
    private final URI address;

    public DataLabsAuthenticationProvider(@Qualifier("normal") RestTemplate restTemplate,
                                          @Value("${datalabs.userPermissions}") String address) {
        this.restTemplate = restTemplate;
        this.address = UriComponentsBuilder.fromHttpUrl(address).build().toUri();
        log.info("Creating {}", this);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (authentication == null) {
            return null;
        }

        val dataLabsUserPermissions =
                this.retrievePermissions(authentication.getCredentials().toString());

        val grantedAuthorities = dataLabsUserPermissions
            .getUserPermissions()
            .stream()
            .map(this::mapDataLabsPermissionsToCatalogueRoles)
            .collect(Collectors.toList());

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

    private DataLabsUserPermissions retrievePermissions(String accessToken) {
        val headers = new HttpHeaders();
        headers.add("authorization", format("bearer %s", accessToken));
        val request = new HttpEntity<>(headers);
        val response = restTemplate.exchange(
                this.address,
                HttpMethod.GET,
                request,
                DataLabsUserPermissions.class
        );
        log.debug("Datalabs user permissions: {}", response.getBody());
        return response.getBody();
    }

    private SimpleGrantedAuthority mapDataLabsPermissionsToCatalogueRoles(String dataLabsPermission) {
        switch (dataLabsPermission) {
            case "system:catalogue:admin":
                return new SimpleGrantedAuthority("CIG_SYSTEM_ADMIN");
            case "system:catalogue:publish":
                return new SimpleGrantedAuthority("ROLE_DATALABS_PUBLISHER");
            case "system:catalogue:edit":
                return new SimpleGrantedAuthority("ROLE_DATALABS_EDITOR");
            default:
                return new SimpleGrantedAuthority(dataLabsPermission);
        }
    }
}
