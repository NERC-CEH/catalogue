package uk.ac.ceh.gateway.catalogue.auth.cognito;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CognitoAuthenticationFilterTest {

    @Mock
    private FilterChain filterChain;

    private CognitoAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CognitoAuthenticationFilter();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateWithValidToken() throws Exception {
        // given
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        // Set up mock OAuth2 user
        var oauth2User = mock(OAuth2User.class);
        var initialAuth = mock(Authentication.class);
        when(initialAuth.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttributes()).thenReturn(Map.of(
            "username", "test-user",
            "email", "test@example.com"
        ));
        when(oauth2User.getAttribute("cognito:groups")).thenReturn(List.of("ROLE_USER", "ROLE_ADMIN"));

        SecurityContextHolder.getContext().setAuthentication(initialAuth);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);

        // Verify authentication object
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertInstanceOf(CognitoAuthenticationToken.class, authentication);

        // Verify principal
        var user = (CatalogueUser) authentication.getPrincipal();
        assertEquals("test-user", user.getUsername());
        assertEquals("test@example.com", user.getEmail());

        // Verify authorities
        var authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        assertTrue(authorities.contains("ROLE_USER"));
        assertTrue(authorities.contains("ROLE_ADMIN"));
    }

    @Test
    void shouldNotAuthenticateWithoutToken() throws Exception {
        // given
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
