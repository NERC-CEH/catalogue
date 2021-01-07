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
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Collectors;

@Slf4j
@ToString
@Service
@Profile("auth:datalabs")
public class DataLabsAuthenticationProvider implements AuthenticationProvider {

    private final RestTemplate restTemplate;
    private final String address;

    public DataLabsAuthenticationProvider(@Qualifier("normal") RestTemplate restTemplate,
                                          @Value("${datalabs.userpermissions}") String address) {
        this.restTemplate = restTemplate;
        this.address = address;
        log.info("Creating {}", this);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (authentication == null) {
            return null;
        }

        DataLabsUserPermissions dataLabsUserPermissions =
                this.retrievePermissions(authentication.getCredentials().toString());

        val grantedAuthorities = dataLabsUserPermissions.getUserPermissions().stream()
                .map(this::mapDataLabsPermissionsToCatalogueRoles)
                .collect(Collectors.toList());

        PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken =
                new PreAuthenticatedAuthenticationToken(authentication.getPrincipal(),
                        authentication.getCredentials(), grantedAuthorities);

        preAuthenticatedAuthenticationToken.setAuthenticated(true);

        return preAuthenticatedAuthenticationToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }

    private DataLabsUserPermissions retrievePermissions(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer" + accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<DataLabsUserPermissions> response = restTemplate.exchange(this.address, HttpMethod.GET,
                request, DataLabsUserPermissions.class);

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