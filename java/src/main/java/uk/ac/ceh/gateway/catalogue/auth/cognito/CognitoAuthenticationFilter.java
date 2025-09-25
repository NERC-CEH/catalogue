package uk.ac.ceh.gateway.catalogue.auth.cognito;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.ac.ceh.gateway.catalogue.model.CatalogueCognitoUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Profile("auth:cognito")
@Component
@RequiredArgsConstructor
public class CognitoAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (!(authentication.getPrincipal() instanceof CatalogueUser)) {
                OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                String username = (String) oauthUser.getAttributes().get("username");
                String email = (String) oauthUser.getAttributes().get("email");
                if (username == null) username = email;
                List<String> groups = oauthUser.getAttribute("cognito:groups");

                CatalogueCognitoUser user = new CatalogueCognitoUser(username, email, groups);
                Authentication cognitoAuth = new CognitoAuthenticationToken(
                    user,
                    new ArrayList<>(),
                    authentication.getCredentials()
                );
                SecurityContextHolder.getContext().setAuthentication(cognitoAuth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
