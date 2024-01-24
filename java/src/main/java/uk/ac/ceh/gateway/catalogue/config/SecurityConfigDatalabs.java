package uk.ac.ceh.gateway.catalogue.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;

import javax.servlet.Filter;

@Slf4j
@Configuration
@Profile("auth:datalabs")
public class SecurityConfigDatalabs {

    @Bean
    @Qualifier("auth")
    public Filter rememberMeAuthenticationFilter(
        AuthenticationProvider authenticationProvider,
        RememberMeServices rememberMeServices
    ) {
        log.info("creating RememberMeAuthenticationFilter");
        return new RememberMeAuthenticationFilter(
                new ProviderManager(authenticationProvider),
                rememberMeServices
        );
    }
}
