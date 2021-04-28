package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.components.userstore.inmemory.InMemoryGroupStore;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import uk.ac.ceh.components.userstore.springsecurity.PreAuthenticatedUsernameAuthenticationProvider;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import javax.servlet.Filter;
import java.util.Collections;

@Slf4j
@TestConfiguration
public class AuthenticationConfig {
    public static final String USER = "test";

    @Bean
    @Qualifier("auth")
    public Filter filter(AuthenticationProvider authenticationProvider) {
        val filter = new RequestHeaderAuthenticationFilter();
        filter.setPrincipalRequestHeader("Remote-User");
        filter.setContinueFilterChainOnUnsuccessfulAuthentication(false);
        filter.setAuthenticationManager(new ProviderManager(Collections.singletonList(authenticationProvider)));
        return filter;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserStore<CatalogueUser> userStore) {
        return new PreAuthenticatedUsernameAuthenticationProvider<>(userStore, new InMemoryGroupStore<>());
    }

    @Bean
    @SneakyThrows
    public UserStore<CatalogueUser> userStore() {
        val userStore = new InMemoryUserStore<CatalogueUser>();
        val test = new CatalogueUser().setUsername(USER);
        userStore.addUser(test, "");
        log.info("{} added to userStore", test.getUsername());
        return userStore;
    }
}
