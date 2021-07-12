package uk.ac.ceh.gateway.catalogue.auth.oidc;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
