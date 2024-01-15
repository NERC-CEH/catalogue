package uk.ac.ceh.gateway.catalogue.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import uk.ac.ceh.components.userstore.springsecurity.AnonymousUserAuthenticationFilter;
import uk.ac.ceh.components.userstore.springsecurity.RestAuthenticationEntryPoint;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import javax.servlet.Filter;

@Slf4j
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    @Qualifier("auth")
    private Filter filter;

    @Override
    protected void configure(AuthenticationManagerBuilder builder) {
        log.info("Configuring authenticationProvider");
        builder.authenticationProvider(authenticationProvider);
    }


    @Override
    public void configure(HttpSecurity http) throws Exception {
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
    }
}
