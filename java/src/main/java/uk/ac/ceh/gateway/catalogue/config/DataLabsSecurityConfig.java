package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import uk.ac.ceh.components.userstore.springsecurity.AnonymousUserAuthenticationFilter;
import uk.ac.ceh.components.userstore.springsecurity.RestAuthenticationEntryPoint;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.Collections;

/**
 * The following spring JavaConfig defines the beans required the spring security
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableWebSecurity
@Profile("auth:datalabs")
public class DataLabsSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired private AuthenticationProvider authenticationProvider;
    @Autowired RememberMeServices rememberMeServices;

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() {
        return new ProviderManager(Collections.singletonList(authenticationProvider));
    }

    @Bean
    public RememberMeAuthenticationFilter rememberMeAuthenticationFilter() {
        return new RememberMeAuthenticationFilter(authenticationManagerBean(), rememberMeServices);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(rememberMeAuthenticationFilter())
                .anonymous()
                .authenticationFilter(new AnonymousUserAuthenticationFilter("NotSure", CatalogueUser.PUBLIC_USER, "ROLE_ANONYMOUS"))
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/**").fullyAuthenticated()
                .antMatchers(HttpMethod.PUT, "/**").fullyAuthenticated()
                .antMatchers(HttpMethod.DELETE, "/**").fullyAuthenticated()
                .and()
                .csrf()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint());
    }
}