package uk.ac.ceh.gateway.catalogue.config;

import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.components.userstore.springsecurity.PreAuthenticatedUsernameAuthenticationProvider;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

@Slf4j
@Configuration
@Profile({"development", "auth-crowd", "test"})
public class SecurityConfigCrowd {

    /**
     * The {@code securityContextRepository} is set explicitly because this filter is built by hand and
     * added to the chain with {@code HttpSecurity.addFilter}, which only records where the filter sits —
     * unlike the built-in configurers it injects nothing. So the filter never receives the
     * {@code RequestAttributeSecurityContextRepository} that {@link SecurityConfig}'s
     * {@code SessionCreationPolicy.STATELESS} installs, and
     * {@code AbstractPreAuthenticatedProcessingFilter}'s default is an
     * {@code HttpSessionSecurityContextRepository} whose {@code saveContext} calls
     * {@code request.getSession(true)}.
     *
     * <p>Without this line the application opened a session on every authenticated request while
     * declaring itself stateless. Nothing ever read those sessions back — under STATELESS the chain's
     * own {@code SecurityContextHolderFilter} reads from the request attribute and session persistence
     * is a {@code NullSecurityContextRepository} — so they bought nothing and cost a session cookie,
     * which the reverse proxy then scoped to the exact request URI rather than {@code /}. A browser
     * accumulated several same-named cookies, sent the stale one first, and the container honoured it:
     * that is how the deposit request reference number came out blank on staging. See dri-one #271, and
     * #272 for the proxy defect itself.</p>
     *
     * @see SecurityConfigDatalabs#rememberMeAuthenticationFilter the other supplier of this bean, with
     * the same defect
     */
    @Bean
    @Qualifier("auth")
    public Filter requestHeaderAuthenticationFilter(AuthenticationManager authenticationManager) {
        RequestHeaderAuthenticationFilter remoteUserFilter = new RequestHeaderAuthenticationFilter();
        remoteUserFilter.setPrincipalRequestHeader("Remote-User");
        remoteUserFilter.setExceptionIfHeaderMissing(false);
        remoteUserFilter.setContinueFilterChainOnUnsuccessfulAuthentication(false);
        remoteUserFilter.setAuthenticationManager(authenticationManager);
        remoteUserFilter.setSecurityContextRepository(new RequestAttributeSecurityContextRepository());
        log.info("Creating RequestHeaderAuthenticationFilter");
        return remoteUserFilter;
    }

    @Bean
    public AuthenticationManager crowdAuthenticationManager(UserStore<CatalogueUser> userStore, GroupStore<CatalogueUser> groupStore) {
        log.info("Creating AuthenticationProvider");
        AuthenticationProvider crowdAuthenticationProvider = new PreAuthenticatedUsernameAuthenticationProvider<>(userStore, groupStore);
        return new ProviderManager(crowdAuthenticationProvider);
    }
}
