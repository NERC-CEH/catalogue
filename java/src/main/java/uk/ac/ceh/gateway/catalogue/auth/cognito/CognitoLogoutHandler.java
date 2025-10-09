package uk.ac.ceh.gateway.catalogue.auth.cognito;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@Profile("auth-cognito")
@Component
@RequiredArgsConstructor
public class CognitoLogoutHandler extends SimpleUrlLogoutSuccessHandler {

    @Value("${aws.cognito.userpool.domain}")
    private String userPoolDomain;

    @Value("${spring.security.oauth2.client.registration.cognito.client-id}")
    private String userPoolClientId;

    @Value("${aws.cognito.client.logout.redirect-uri}")
    private String logoutRedirectUrl;

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        return UriComponentsBuilder
            .fromUri(URI.create(userPoolDomain + "/logout"))
            .queryParam("client_id", userPoolClientId)
            .queryParam("logout_uri", logoutRedirectUrl)
            .encode(StandardCharsets.UTF_8)
            .build()
            .toUriString();
    }
}
