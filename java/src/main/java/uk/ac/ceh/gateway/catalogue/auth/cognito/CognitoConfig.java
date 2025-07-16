package uk.ac.ceh.gateway.catalogue.auth.cognito;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.net.MalformedURLException;
import java.net.URL;

@Configuration
@Profile("auth:cognito")
public class CognitoConfig {

    @Value("${cognito.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${cognito.connection-timeout:5000}")
    private int connectionTimeout;

    @Value("${cognito.read-timeout:5000}")
    private int readTimeout;

    @Bean
    public JWKSource<SecurityContext> jwkSource() throws MalformedURLException {
        ResourceRetriever resourceRetriever = new DefaultResourceRetriever(
            connectionTimeout,
            readTimeout,
            0  // size limit, 0 means no limit
        );

        return new RemoteJWKSet<>(
            new URL(jwkSetUri),
            resourceRetriever
        );
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
            com.nimbusds.jose.JWSAlgorithm.RS256,
            jwkSource
        );
        jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {
            // Basic claims verification is handled by Spring Security
            // Additional claims verification can be added here if needed
        });

        return new NimbusJwtDecoder(jwtProcessor);
    }
}
