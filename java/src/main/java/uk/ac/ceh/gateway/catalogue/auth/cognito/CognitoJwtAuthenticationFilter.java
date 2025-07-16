package uk.ac.ceh.gateway.catalogue.auth.cognito;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class CognitoJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtDecoder jwtDecoder;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = resolveToken(request);
            if (StringUtils.hasText(jwt) && validateToken(jwt)) {
                Jwt decodedJwt = jwtDecoder.decode(jwt);
                Authentication authentication = createAuthentication(decodedJwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
//                log.debug("Set Authentication to security context for user: {}", authentication.getName());
                log.info("Set Authentication to security context for user: {}", authentication.getName());     // rex debug
            }
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Failed to validate JWT token: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private boolean validateToken(String authToken) {
        try {
            JWT jwt = JWTParser.parse(authToken);
            if (jwt instanceof SignedJWT) {
                return true;
            }
            log.warn("JWT is not a signed token");
            return false;
        } catch (ParseException e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            return false;
        }
    }

    private Authentication createAuthentication(Jwt jwt) {
        String username = jwt.getSubject();
        Collection<? extends GrantedAuthority> authorities = extractAuthorities(jwt);
        return new UsernamePasswordAuthenticationToken(username, jwt, authorities);
    }

    private Collection<? extends GrantedAuthority> extractAuthorities(Jwt jwt) {
        try {
            List<String> groups = jwt.getClaim("cognito:groups");
            if (groups != null) {
//                return groups.stream()
//                    .map(group -> new SimpleGrantedAuthority("ROLE_" + group.toUpperCase()))
//                    .collect(Collectors.toList());
                return groups.stream()
                    .map(group -> {
                        String role = "ROLE_" + group.toUpperCase();
//                        log.debug("Mapping Cognito group '{}' to role '{}'", group, role);
                        log.info("Filter Mapping Cognito group '{}' to role '{}'", group, role);   // rex debug
                        return new SimpleGrantedAuthority(role);
                    })
                    .collect(Collectors.toList());

            }
        } catch (Exception e) {
            log.warn("Failed to extract authorities from JWT: {}", e.getMessage());
        }
        return Collections.emptyList();
    }
}
