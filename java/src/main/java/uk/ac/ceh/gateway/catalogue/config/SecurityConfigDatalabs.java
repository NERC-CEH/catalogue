package uk.ac.ceh.gateway.catalogue.config;

import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

@Slf4j
@Configuration
@Profile("auth-datalabs")
public class SecurityConfigDatalabs {

    /**
     * The Datalabs half of the same defect described on
     * {@link SecurityConfigCrowd#requestHeaderAuthenticationFilter}: this filter is also supplied under
     * {@code @Qualifier("auth")} and added to {@link SecurityConfig}'s STATELESS chain by
     * {@code HttpSecurity.addFilter}, and {@code RememberMeAuthenticationFilter} carries the same
     * {@code HttpSessionSecurityContextRepository} default. Setting the repository explicitly is what
     * makes the declared statelessness true under {@code auth-datalabs} as well.
     */
    @Bean
    @Qualifier("auth")
    public Filter rememberMeAuthenticationFilter(
        AuthenticationProvider authenticationProvider,
        RememberMeServices rememberMeServices
    ) {
        log.info("creating RememberMeAuthenticationFilter");
        var filter = new RememberMeAuthenticationFilter(
                new ProviderManager(authenticationProvider),
                rememberMeServices
        );
        filter.setSecurityContextRepository(new RequestAttributeSecurityContextRepository());
        return filter;
    }
}
