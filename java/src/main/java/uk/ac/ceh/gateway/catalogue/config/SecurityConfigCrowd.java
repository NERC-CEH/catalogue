package uk.ac.ceh.gateway.catalogue.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.components.userstore.springsecurity.PreAuthenticatedUsernameAuthenticationProvider;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import javax.servlet.Filter;

@Slf4j
@Configuration
@Profile({"development", "auth:crowd", "test"})
public class SecurityConfigCrowd {

    @Bean
    @Qualifier("auth")
    public Filter requestHeaderAuthenticationFilter(AuthenticationManager authenticationManager) {
        RequestHeaderAuthenticationFilter remoteUserFilter = new RequestHeaderAuthenticationFilter();
        remoteUserFilter.setPrincipalRequestHeader("Remote-User");
        remoteUserFilter.setExceptionIfHeaderMissing(false);
        remoteUserFilter.setContinueFilterChainOnUnsuccessfulAuthentication(false);
        remoteUserFilter.setAuthenticationManager(authenticationManager);
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
