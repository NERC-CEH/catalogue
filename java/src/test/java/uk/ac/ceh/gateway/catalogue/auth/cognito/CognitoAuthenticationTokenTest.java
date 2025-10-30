package uk.ac.ceh.gateway.catalogue.auth.cognito;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CognitoAuthenticationTokenTest {

    @Test
    void shouldCreateAuthenticatedToken() {
        // given
        var principal = new CatalogueUser("test-user", "test@test.com");
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // when
        var token = new CognitoAuthenticationToken(principal, authorities, null);

        // then
        assertTrue(token.isAuthenticated());
        assertEquals(principal, token.getPrincipal());
        assertEquals(authorities, token.getAuthorities());
        assertNull(token.getCredentials());
    }
}
