package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.components.userstore.springsecurity.AnonymousUserAuthenticationFilter;
import uk.ac.ceh.components.userstore.springsecurity.PreAuthenticatedUsernameAuthenticationProvider;
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
    @Autowired private UserStore<CatalogueUser> userStore;
    @Autowired private GroupStore<CatalogueUser> groupStore;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private Environment environment;
    @Autowired RememberMeServices rememberMeServices;

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() {
        return new ProviderManager(Collections.singletonList(new PreAuthenticatedUsernameAuthenticationProvider<>(userStore, groupStore)));
    }

    @Bean
    public RememberMeAuthenticationFilter rememberMeAuthenticationFilter() {
        return new RememberMeAuthenticationFilter(authenticationManager, rememberMeServices);
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