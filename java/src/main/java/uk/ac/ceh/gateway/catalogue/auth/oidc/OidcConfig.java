package uk.ac.ceh.gateway.catalogue.auth.oidc;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.URI;

@Configuration
@Profile("auth:datalabs")
public class OidcConfig {
    @Value("${datalabs.remoteJwks}")
    private String remoteJwks;

    @Bean
    @SneakyThrows
    public JWKSource<SecurityContext> keySource() {
        return new RemoteJWKSet<>(new URI(remoteJwks).toURL());
    }
}
