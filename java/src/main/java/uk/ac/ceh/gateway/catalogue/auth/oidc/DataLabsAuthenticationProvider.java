package uk.ac.ceh.gateway.catalogue.auth.oidc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class DataLabsAuthenticationProvider implements AuthenticationProvider {

    private final RestTemplate restTemplate;
    private final String address;

    public DataLabsAuthenticationProvider(@Qualifier("normal") RestTemplate restTemplate,
                                          @Value("${datalabs.userpermissions}") String address) {
        this.restTemplate = restTemplate;
        this.address = address;
        log.info("Creating {}", this);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (authentication == null) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        String username = (String)authentication.getPrincipal();
        headers.add("Authorization", "Bearer" + authentication.getCredentials().toString());

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<DataLabsUserPermissions> response = restTemplate.exchange(this.address, HttpMethod.GET,
                request, DataLabsUserPermissions.class);

        return new PreAuthenticatedAuthenticationToken(username, response.getBody());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }

}