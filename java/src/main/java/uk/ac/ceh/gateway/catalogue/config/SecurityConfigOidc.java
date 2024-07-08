package uk.ac.ceh.gateway.catalogue.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import uk.ac.ceh.components.userstore.springsecurity.AnonymousUserAuthenticationFilter;
import uk.ac.ceh.gateway.catalogue.auth.oidc.CatalogueUserOidcUserService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@Profile("auth:oidc")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
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
        return http
            .cors(withDefaults()) // Enable Cross-Origin Resource Sharing (CORS)
            .anonymous(anonymous -> // Allow anonymous access
                anonymous.authenticationFilter(new AnonymousUserAuthenticationFilter("NotSure", CatalogueUser.PUBLIC_USER, "ROLE_ANONYMOUS"))
            )
            .authorizeHttpRequests((authorizeRequests) -> authorizeRequests
                .requestMatchers(HttpMethod.POST, "/**").fullyAuthenticated() // Require full authentication for POST requests
                .requestMatchers(HttpMethod.PUT, "/**").fullyAuthenticated() // Require full authentication for PUT requests
                .requestMatchers(HttpMethod.DELETE, "/**").fullyAuthenticated() // Require full authentication for DELETE requests
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(
                    userInfo -> userInfo
                        .oidcUserService(new CatalogueUserOidcUserService()) // Set OIDC user service for OAuth2 login
                )
            )
            .csrf(AbstractHttpConfigurer::disable) // Disable Cross-Site Request Forgery (CSRF) protection
            .build();
    }

}
