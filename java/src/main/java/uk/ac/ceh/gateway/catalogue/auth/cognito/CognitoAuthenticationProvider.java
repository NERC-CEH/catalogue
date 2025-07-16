package uk.ac.ceh.gateway.catalogue.auth.cognito;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Profile("auth:cognito")
public class CognitoAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication == null) {
            return null;
        }

        if (!(authentication.getCredentials() instanceof Jwt)) {
            return authentication;
        }

        Jwt jwt = (Jwt) authentication.getCredentials();
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        return new JwtAuthenticationToken(jwt, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        try {
            List<String> groups = jwt.getClaim("cognito:groups");
            if (groups != null && !groups.isEmpty()) {
                return groups.stream()
                    .map(group -> {
                        String role = "ROLE_" + group.toUpperCase();
//                        log.debug("Mapping Cognito group '{}' to role '{}'", group, role);
                        log.info("Provider Mapping Cognito group '{}' to role '{}'", group, role);   // rex debug
                        return new SimpleGrantedAuthority(role);
                    })
                    .collect(Collectors.toList());
            }
            log.warn("No groups found in JWT claims for user: {}", jwt.getSubject());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to extract authorities from JWT: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
