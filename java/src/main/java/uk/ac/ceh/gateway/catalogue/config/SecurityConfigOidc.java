package uk.ac.ceh.gateway.catalogue.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import uk.ac.ceh.components.userstore.springsecurity.AnonymousUserAuthenticationFilter;
import uk.ac.ceh.gateway.catalogue.auth.oidc.CatalogueUserOidcUserService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

@Slf4j
@Profile("auth:oidc")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfigOidc {

    /**
     * Configures the security filter chain for HTTP requests when using OIDC.
     *
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors() // Enable Cross-Origin Resource Sharing (CORS)
            .and()
            .anonymous() // Allow anonymous access
            .authenticationFilter(new AnonymousUserAuthenticationFilter("NotSure", CatalogueUser.PUBLIC_USER, "ROLE_ANONYMOUS"))
            .and()
            .authorizeRequests((authorizeRequests) -> authorizeRequests
                .mvcMatchers(HttpMethod.POST, "/**").fullyAuthenticated() // Require full authentication for POST requests
                .mvcMatchers(HttpMethod.PUT, "/**").fullyAuthenticated() // Require full authentication for PUT requests
                .mvcMatchers(HttpMethod.DELETE, "/**").fullyAuthenticated() // Require full authentication for DELETE requests
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(
                    userInfo -> userInfo
                        .oidcUserService(new CatalogueUserOidcUserService()) // Set OIDC user service for OAuth2 login
                )
            )
            .csrf() // Disable Cross-Site Request Forgery (CSRF) protection
            .disable();
        return http.build();
    }

}
