package uk.ac.ceh.gateway.catalogue.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import uk.ac.ceh.components.userstore.springsecurity.AnonymousUserAuthenticationFilter;
import uk.ac.ceh.gateway.catalogue.auth.cognito.CognitoAuthenticationFilter;
import uk.ac.ceh.gateway.catalogue.auth.cognito.CognitoLogoutHandler;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@Profile("auth-cognito")
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfigCognito {

    private final CognitoAuthenticationFilter cognitoJwtAuthenticationFilter;
    private final CognitoLogoutHandler cognitoLogoutHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .cors(withDefaults())
            .anonymous(anonymous -> anonymous
                .authenticationFilter(new AnonymousUserAuthenticationFilter("NotSure", CatalogueUser.PUBLIC_USER, "ROLE_ANONYMOUS"))
            )
            .authorizeHttpRequests((authorizeRequests) -> authorizeRequests
                .requestMatchers(HttpMethod.POST, "/**").fullyAuthenticated()
                .requestMatchers(HttpMethod.PUT, "/**").fullyAuthenticated()
                .requestMatchers(HttpMethod.DELETE, "/**").fullyAuthenticated()
                .anyRequest().permitAll()
            )
            .oauth2Login(Customizer.withDefaults())
            .addFilterBefore(cognitoJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .logout(logout -> logout
                .logoutSuccessHandler(cognitoLogoutHandler)
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            )
            // See AdminDeleteCsrfCustomizer: CSRF stays off everywhere except the admin delete route,
            // shared with SecurityConfig/SecurityConfigOidc so the filter chains cannot drift apart.
            .csrf(AdminDeleteCsrfCustomizer::configure)
            .build();
    }
}
