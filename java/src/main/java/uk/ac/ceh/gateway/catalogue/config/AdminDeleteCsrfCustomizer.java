package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

/**
 * The CSRF customiser for {@code /maintenance/documents/delete/**}, shared by every auth profile's
 * {@code SecurityFilterChain}.
 *
 * <p>This exists as its own class, rather than being repeated inline in {@link SecurityConfig},
 * {@link SecurityConfigOidc} and {@link SecurityConfigCognito}, because those three classes each define
 * an independent filter chain and there is nothing that forces them to stay in sync. That is exactly how
 * this route ended up CSRF-protected under Crowd/Datalabs auth but not under OIDC or Cognito auth: the fix
 * was applied to one chain and never copied to the other two. Sharing one method removes the chance of
 * that happening again — every profile calls the same code, so there is nothing to keep in sync.</p>
 *
 * <p>CSRF stays off everywhere else. The admin delete route can remove any record in any catalogue, so a
 * forged POST from an authenticated administrator's browser would otherwise be enough to destroy a record
 * whose id the attacker knows — and the retyped-id confirmation is no defence, since a forger supplies
 * both fields.</p>
 *
 * <p>{@code requireCsrfProtectionMatcher} narrows enforcement to just this path, so every existing form and
 * API is unaffected. A cookie repository is required rather than the default: session creation is
 * STATELESS, so {@code HttpSessionCsrfTokenRepository} would have nowhere to keep the token. {@code httpOnly}
 * is left at its default ({@code true}) because the token is rendered server-side into a hidden field, so
 * nothing needs to read it from JavaScript.</p>
 *
 * <p>The no-op {@code sessionAuthenticationStrategy} is what makes the cookie usable at all. By default CSRF
 * installs {@code CsrfAuthenticationStrategy}, which on authentication deletes the stored token and only
 * <em>defers</em> generating its replacement. Authentication in this application is per-request —
 * {@code RequestHeaderAuthenticationFilter} (or the OIDC/Cognito equivalents) re-authenticates on every
 * request, and none of the filter chains persist a security context between requests, so
 * {@code SessionManagementFilter} treats each request as a fresh login. Any authenticated request that does
 * not itself read a token therefore leaves the browser with no cookie: loading the delete form deletes its
 * own token again as soon as the page's stylesheet and logo are fetched, and the POST is then rejected.
 * Rotating on authentication buys nothing when every request is an authentication.</p>
 */
final class AdminDeleteCsrfCustomizer {

    private AdminDeleteCsrfCustomizer() {
    }

    static void configure(CsrfConfigurer<HttpSecurity> csrf) {
        csrf
            .csrfTokenRepository(new CookieCsrfTokenRepository())
            .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
            .requireCsrfProtectionMatcher(
                pathPattern(HttpMethod.POST, "/maintenance/documents/delete/**"));
    }
}
