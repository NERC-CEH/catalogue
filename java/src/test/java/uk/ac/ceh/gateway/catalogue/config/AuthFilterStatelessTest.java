package uk.ac.ceh.gateway.catalogue.config;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.ADMIN;

/**
 * {@link SecurityConfig} declares {@link org.springframework.security.config.http.SessionCreationPolicy#STATELESS},
 * yet the application used to open an {@code HttpSession} on every authenticated request — including
 * page views that never touch one. This pins that shut for both filters that chain consumes.
 *
 * <p>Two framework facts combined to cause it. {@code HttpSecurity.addFilter(Filter)} only records where
 * a filter sits in the chain; unlike the built-in configurers it injects nothing into the filter, so a
 * hand-built {@code @Bean} filter never receives the {@code RequestAttributeSecurityContextRepository}
 * that STATELESS installs. And both filters supplied under {@code @Qualifier("auth")} default to an
 * {@code HttpSessionSecurityContextRepository}
 * ({@code AbstractPreAuthenticatedProcessingFilter} line 124, {@code RememberMeAuthenticationFilter}
 * line 85, spring-security-web 7.0.4), whose {@code saveContext} calls {@code request.getSession(true)}
 * for any non-anonymous context.</p>
 *
 * <p>The sessions were never read back — under STATELESS the chain's {@code SecurityContextHolderFilter}
 * is given a {@code RequestAttributeSecurityContextRepository} and session persistence becomes a
 * {@code NullSecurityContextRepository} — so they cost memory and, worse, a session cookie which the
 * reverse proxy then mis-scopes to the exact request URI. That is what made the deposit request
 * reference number vanish on staging (GitLab MR !1155, dri-one #271, #272).</p>
 *
 * <p>These tests drive each filter directly against a {@code MockHttpServletRequest} rather than booting
 * a filter chain: a session is either created on that request or it is not, which is the whole assertion.
 * {@code MockMvc} cannot see this — it never surfaced the bug — and the {@code auth-datalabs} filter
 * cannot be reached end to end without real remember-me infrastructure. Each filter also gets a
 * companion test proving authentication still succeeds, so "no session" cannot be passed by a filter
 * that has simply stopped working.</p>
 */
@DisplayName("The authentication filters feeding the STATELESS filter chain")
class AuthFilterStatelessTest {

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final RequestAttributeSecurityContextRepository requestAttributeRepository =
        new RequestAttributeSecurityContextRepository();

    /**
     * The filters consult {@code SecurityContextHolder} before authenticating, and it is thread-bound,
     * so leaving a context behind would let one test decide another's outcome.
     */
    @AfterEach
    void clearTheSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("RequestHeaderAuthenticationFilter (development, auth-crowd, test)")
    class Crowd {

        private final Filter filter = new SecurityConfigCrowd()
            .requestHeaderAuthenticationFilter(authenticatesAs(
                new PreAuthenticatedAuthenticationToken(ADMIN, "n/a", List.of(new SimpleGrantedAuthority("ROLE_USER")))));

        @Test
        @DisplayName("opens no session when it authenticates a request")
        void opensNoSession() throws Exception {
            request.addHeader("Remote-User", ADMIN);

            filter.doFilter(request, response, new MockFilterChain());

            assertThat("an authenticated request must not open a session",
                request.getSession(false), is(nullValue()));
        }

        @Test
        @DisplayName("still authenticates, into the repository the STATELESS chain reads")
        void stillAuthenticates() throws Exception {
            request.addHeader("Remote-User", ADMIN);

            filter.doFilter(request, response, new MockFilterChain());

            assertThat(SecurityContextHolder.getContext().getAuthentication(), is(notNullValue()));
            assertThat("the context must land where the chain's SecurityContextHolderFilter looks for it",
                requestAttributeRepository.loadDeferredContext(request).get().getAuthentication(),
                is(notNullValue()));
        }
    }

    @Nested
    @DisplayName("RememberMeAuthenticationFilter (auth-datalabs)")
    class Datalabs {

        private final RememberMeAuthenticationToken rememberMeToken = new RememberMeAuthenticationToken(
            "key", ADMIN, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        private Filter filter() {
            var rememberMeServices = mock(RememberMeServices.class);
            given(rememberMeServices.autoLogin(any(), any())).willReturn(rememberMeToken);
            return new SecurityConfigDatalabs()
                .rememberMeAuthenticationFilter(remembers(rememberMeToken), rememberMeServices);
        }

        @Test
        @DisplayName("opens no session when it authenticates a request")
        void opensNoSession() throws Exception {
            filter().doFilter(request, response, new MockFilterChain());

            assertThat("an authenticated request must not open a session",
                request.getSession(false), is(nullValue()));
        }

        @Test
        @DisplayName("still authenticates, into the repository the STATELESS chain reads")
        void stillAuthenticates() throws Exception {
            filter().doFilter(request, response, new MockFilterChain());

            assertThat(SecurityContextHolder.getContext().getAuthentication(), is(notNullValue()));
            assertThat("the context must land where the chain's SecurityContextHolderFilter looks for it",
                requestAttributeRepository.loadDeferredContext(request).get().getAuthentication(),
                is(notNullValue()));
        }
    }

    private static AuthenticationManager authenticatesAs(Authentication authenticated) {
        return authentication -> authenticated;
    }

    /**
     * Stands in for the real Datalabs provider: the filter hands its remember-me token to a
     * {@code ProviderManager}, which needs a provider that both claims the token type and returns
     * something authenticated.
     */
    private static AuthenticationProvider remembers(RememberMeAuthenticationToken token) {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) {
                return token;
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return RememberMeAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }
}
