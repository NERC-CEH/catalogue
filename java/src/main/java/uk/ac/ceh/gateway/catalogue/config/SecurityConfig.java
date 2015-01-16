package uk.ac.ceh.gateway.catalogue.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.components.userstore.springsecurity.AnonymousUserAuthenticationFilter;
import uk.ac.ceh.components.userstore.springsecurity.PreAuthenticatedUsernameAuthenticationProvider;
import uk.ac.ceh.components.userstore.springsecurity.RestAuthenticationEntryPoint;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

/**
 * The following spring JavaConfig defines the beans required the spring security
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebMvcSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired UserStore<CatalogueUser> userstore;
    @Autowired GroupStore<CatalogueUser> groupstore;
    
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() {
        return new ProviderManager(Arrays.asList(new PreAuthenticatedUsernameAuthenticationProvider(userstore, groupstore)));
    }
    
    @Bean
    public RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter() {
        RequestHeaderAuthenticationFilter remoteUserFilter = new RequestHeaderAuthenticationFilter();
        remoteUserFilter.setPrincipalRequestHeader("Remote-User");
        remoteUserFilter.setExceptionIfHeaderMissing(false);
        remoteUserFilter.setContinueFilterChainOnUnsuccessfulAuthentication(false);
        remoteUserFilter.setAuthenticationManager(authenticationManagerBean());
        return remoteUserFilter;
    }
    
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .addFilter(requestHeaderAuthenticationFilter())
            .anonymous()
                .authenticationFilter(new AnonymousUserAuthenticationFilter("NotSure", CatalogueUser.PUBLIC_USER, "ROLE_ANONYMOUS"))
            .and()
            .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/**").fullyAuthenticated()
                .antMatchers(HttpMethod.PUT, "/**").fullyAuthenticated()
                .antMatchers(HttpMethod.DELETE, "/**").fullyAuthenticated()
            .and()
            .requiresChannel()
                .antMatchers(HttpMethod.POST, "/**").requiresSecure()
                .antMatchers(HttpMethod.PUT, "/**").requiresSecure()
                .antMatchers(HttpMethod.DELETE, "/**").requiresSecure()
            .and()
            .csrf()
                .disable()
            .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint());
    }
}