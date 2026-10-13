package uk.ac.ceh.gateway.catalogue.csp;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Sends the {@link ContentSecurityPolicy} on every response, with a fresh nonce for inline scripts.
 *
 * <p>A servlet filter rather than a {@code headers()} customiser on the security filter chain: each auth
 * profile builds its own chain, and this applies under all of them with nothing to keep in step. The
 * nonce is a request attribute, which templates read as {@code ${csp.nonce}} ({@link CspNonce}).</p>
 *
 * <p>Report-only for now (NERC-CEH/dri-one#454): browsers report what the policy would block, to
 * {@link CspReportController}, and block nothing. Switching the header name enforces it.</p>
 *
 * <p>Ordered just ahead of Spring Security, so the responses it refuses or redirects carry the policy too.</p>
 */
@Component
@Order(SecurityFilterProperties.DEFAULT_FILTER_ORDER - 1)
public class ContentSecurityPolicyFilter extends OncePerRequestFilter {

    static final String HEADER = "Content-Security-Policy-Report-Only";
    static final String NONCE_ATTRIBUTE = ContentSecurityPolicyFilter.class.getName() + ".nonce";
    /** The Reporting API group the policy's report-to names, declared in a Reporting-Endpoints header. */
    static final String REPORT_GROUP = "csp";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final ContentSecurityPolicy policy;

    public ContentSecurityPolicyFilter(@Value("${fuseki.sparqlEndpoint}") String sparqlEndpoint) {
        this.policy = new ContentSecurityPolicy(sparqlEndpoint, CspReportController.PATH);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String nonce = newNonce();
        request.setAttribute(NONCE_ATTRIBUTE, nonce);
        response.setHeader(HEADER, policy.withNonce(nonce));
        response.setHeader("Reporting-Endpoints", REPORT_GROUP + "=\"" + CspReportController.PATH + "\"");
        chain.doFilter(request, response);
    }

    private static String newNonce() {
        byte[] bytes = new byte[18];
        RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
