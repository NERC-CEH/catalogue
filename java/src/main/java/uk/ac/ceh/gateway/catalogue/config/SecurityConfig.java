package uk.ac.ceh.gateway.catalogue.config;

import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
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
 * xml configuration
 * @author cjohn
 */
@Configuration
@ImportResource("classpath:/uk/ac/ceh/gateway/catalogue/config/securityContext-config.xml")
public class SecurityConfig {
    @Autowired UserStore<CatalogueUser> userstore;
    @Autowired GroupStore<CatalogueUser> groupstore;
    @Value("${sso.token.domain}") String cookieDomain;
    @Value("${sso.token.ttl}") Integer cookieMaxAge;
    @Value("${sso.token.key}") String cookieName;
    @Value("${sso.token.keystore.location}") private String keystoreLocation;
    @Value("${sso.token.keystore.password}") private String keystorePassword;
    
    @Bean
    public UsernamePasswordAuthenticationProvider authenticationProvider() {
        return new UsernamePasswordAuthenticationProvider(userstore, groupstore);
    }
    
    @Bean
    public AnonymousAuthenticationFilter anonymousAuthenticationFilter() {
       return new AnonymousUserAuthenticationFilter("NotSure", CatalogueUser.PUBLIC_USER, "ROLE_ANONYMOUS");
    }
    
    @Bean
    public TokenGenerator resetCredentialsTokenGenerator() throws StatelessTokenKeystoreManagerException {
        return new StatelessTokenGenerator(
                 new StatelessTokenKeystoreManager(new File(keystoreLocation), 
                                keystorePassword.toCharArray(), "reset-hmac", "reset-key"));
    }
    
    @Bean
    public TokenRememberMeServices<CatalogueUser> rememberMeServices() throws StatelessTokenKeystoreManagerException {
        CookieGenerator cookieGenerator = new CookieGenerator();
        cookieGenerator.setCookieDomain(cookieDomain);
        cookieGenerator.setCookieMaxAge(cookieMaxAge);
        cookieGenerator.setCookieName(cookieName);
        
        TokenGenerator tokenGenerator = new StatelessTokenGenerator(
                                new StatelessTokenKeystoreManager(new File(keystoreLocation), 
                                keystorePassword.toCharArray()));
        
        return new TokenRememberMeServices<>("CehSecuredKey", userstore, groupstore, tokenGenerator, cookieGenerator);
    }   
    
    @Bean 
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }
}