package uk.ac.ceh.gateway.catalogue.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import uk.ac.ceh.components.userstore.springsecurity.AnonymousUserAuthenticationFilter;
import uk.ac.ceh.components.userstore.springsecurity.RestAuthenticationEntryPoint;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import javax.servlet.Filter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    @Autowired
    @Qualifier("auth")
    private Filter filter;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors()
            .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilter(filter)
            .anonymous()
                .authenticationFilter(new AnonymousUserAuthenticationFilter("NotSure", CatalogueUser.PUBLIC_USER, "ROLE_ANONYMOUS"))
            .and()
            .authorizeRequests()
                .mvcMatchers(HttpMethod.POST, "/**").fullyAuthenticated()
                .mvcMatchers(HttpMethod.PUT, "/**").fullyAuthenticated()
                .mvcMatchers(HttpMethod.DELETE, "/**").fullyAuthenticated()
            .and()
            .csrf()
                .disable()
            .exceptionHandling()
            .authenticationEntryPoint(new RestAuthenticationEntryPoint());
        return http.build();
    }
}
