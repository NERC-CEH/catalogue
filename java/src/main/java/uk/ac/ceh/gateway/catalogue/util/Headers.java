package uk.ac.ceh.gateway.catalogue.util;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpHeaders;

import java.util.Objects;

public class Headers {
    public static HttpHeaders withBasicAuth(String username, String password) {
        try {
            Objects.requireNonNull(username, "Username cannot be null");
            Objects.requireNonNull(password, "Password cannot be null");
            if (username.isEmpty()) throw new IllegalArgumentException("Username cannot be empty");
            if (password.isEmpty()) throw new IllegalArgumentException("Password cannot be empty");
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException(ex);
        }
        String plainCreds = username + ':' + password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
        return headers;
    }

    public static HttpHeaders withBearerToken(String token) {
        try {
            Objects.requireNonNull(token, "Token cannot be null");
            if (token.isBlank()) throw new IllegalArgumentException("Token cannot be empty");
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException(ex);
        }
        HttpHeaders headers = new HttpHeaders();
        // Strip surrounding whitespace before building the header. Secrets sourced from
        // files or Kubernetes Secrets often carry a trailing newline, and the JDK HTTP
        // client rejects any CR/LF in a header value (header-injection guard), which would
        // otherwise surface as an IllegalArgumentException / 500 at request time.
        headers.setBearerAuth(token.strip());
        return headers;
    }
}
