package uk.ac.ceh.gateway.catalogue.config;

import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import uk.ac.ceh.components.userstore.springsecurity.AnonymousUserAuthenticationFilter;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

@Slf4j
@Profile("!auth:oidc & !auth-cognito")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Autowired
    @Qualifier("auth")
    private Filter filter;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .cors(withDefaults())
            .sessionManagement(sessionManagement -> sessionManagement
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilter(filter)
            .anonymous(anonymous -> anonymous
                .authenticationFilter(new AnonymousUserAuthenticationFilter("NotSure", CatalogueUser.PUBLIC_USER, "ROLE_ANONYMOUS"))
            )
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers(HttpMethod.POST, "/**").fullyAuthenticated()
                .requestMatchers(HttpMethod.PUT, "/**").fullyAuthenticated()
                .requestMatchers(HttpMethod.DELETE, "/**").fullyAuthenticated()
                .anyRequest().permitAll()
            )
            // CSRF stays off everywhere except the admin delete route. That route can remove any record
            // in any catalogue, so a forged POST from an authenticated administrator's browser would
            // otherwise be enough to destroy a record whose id the attacker knows — and the retyped-id
            // confirmation is no defence, since a forger supplies both fields.
            //
            // requireCsrfProtectionMatcher narrows enforcement to just this path, so every existing form
            // and API is unaffected. A cookie repository is required rather than the default: session
            // creation is STATELESS above, so HttpSessionCsrfTokenRepository would have nowhere to keep
            // the token. httpOnly is left at its default (true) because the token is rendered
            // server-side into a hidden field, so nothing needs to read it from JavaScript.
            //
            // The no-op sessionAuthenticationStrategy is what makes the cookie usable at all. By default
            // CSRF installs CsrfAuthenticationStrategy, which on authentication deletes the stored token
            // and only *defers* generating its replacement. Authentication here is per-request —
            // RequestHeaderAuthenticationFilter re-reads Remote-User on every request, and STATELESS
            // means no security context is ever persisted, so SessionManagementFilter treats each
            // request as a fresh login. Any authenticated request that does not itself read a token
            // therefore leaves the browser with no cookie: loading the delete form deletes its own token
            // again as soon as the page's stylesheet and logo are fetched, and the POST is then rejected.
            // Rotating on authentication buys nothing when every request is an authentication.
            .csrf(csrf -> csrf
                .csrfTokenRepository(new CookieCsrfTokenRepository())
                .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
                .requireCsrfProtectionMatcher(
                    pathPattern(HttpMethod.POST, "/maintenance/documents/delete/**")))
            .build();
    }
}
