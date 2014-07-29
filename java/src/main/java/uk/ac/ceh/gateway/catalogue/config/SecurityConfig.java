package uk.ac.ceh.gateway.catalogue.config;

import java.io.File;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.util.CookieGenerator;
import uk.ac.ceh.components.tokengeneration.TokenGenerator;
import uk.ac.ceh.components.tokengeneration.stateless.StatelessTokenGenerator;
import uk.ac.ceh.components.tokengeneration.stateless.StatelessTokenKeystoreManager;
import uk.ac.ceh.components.tokengeneration.stateless.StatelessTokenKeystoreManagerException;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.components.userstore.springsecurity.AnonymousUserAuthenticationFilter;
import uk.ac.ceh.components.userstore.springsecurity.RestAuthenticationEntryPoint;
import uk.ac.ceh.components.userstore.springsecurity.TokenRememberMeServices;
import uk.ac.ceh.components.userstore.springsecurity.UsernamePasswordAuthenticationProvider;
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
    @Value("${sso.token.domain}") String cookieDomain;
    @Value("${sso.token.ttl}") Integer cookieMaxAge;
    @Value("${sso.token.key}") String cookieName;
    @Value("${sso.token.keystore.location}") private String keystoreLocation;
    @Value("${sso.token.keystore.password}") private String keystorePassword;
    
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() {
        return new ProviderManager(Arrays.asList(new UsernamePasswordAuthenticationProvider(userstore, groupstore)));
    }
    
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .anonymous()
                .authenticationFilter(new AnonymousUserAuthenticationFilter("NotSure", CatalogueUser.PUBLIC_USER, "ROLE_ANONYMOUS"))
            .and()
            .rememberMe()
                .key("CehSecuredKey")
                .rememberMeServices(rememberMeServices())
            .and()
            .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint());
    }
    
    @Bean
    public TokenGenerator resetCredentialsTokenGenerator() throws StatelessTokenKeystoreManagerException {
        return new StatelessTokenGenerator(
                 new StatelessTokenKeystoreManager(new File(keystoreLocation), 
                                keystorePassword.toCharArray(), "reset-hmac", "reset-key"));
    }
    
    private TokenRememberMeServices<CatalogueUser> rememberMeServices() throws StatelessTokenKeystoreManagerException {
        CookieGenerator cookieGenerator = new CookieGenerator();
        cookieGenerator.setCookieDomain(cookieDomain);
        cookieGenerator.setCookieMaxAge(cookieMaxAge);
        cookieGenerator.setCookieName(cookieName);
        
        TokenGenerator tokenGenerator = new StatelessTokenGenerator(
                                new StatelessTokenKeystoreManager(new File(keystoreLocation), 
                                keystorePassword.toCharArray()));
        
        return new TokenRememberMeServices<>("CehSecuredKey", userstore, groupstore, tokenGenerator, cookieGenerator);
    }   
}