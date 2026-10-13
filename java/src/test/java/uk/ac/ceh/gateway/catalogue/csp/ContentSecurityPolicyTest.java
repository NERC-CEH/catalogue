package uk.ac.ceh.gateway.catalogue.csp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ContentSecurityPolicy")
class ContentSecurityPolicyTest {

    private final ContentSecurityPolicy policy =
        new ContentSecurityPolicy("https://sparql.example.org/ds/sparql", "/csp-reports");

    private Map<String, String> directives(String nonce) {
        return Arrays.stream(policy.withNonce(nonce).split("; "))
            .map(directive -> directive.split(" ", 2))
            .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));
    }

    @Test
    @DisplayName("allows inline scripts only with the request's nonce")
    void inlineScriptsNeedTheNonce() {
        String scripts = directives("abc123").get("script-src");

        assertThat(scripts).contains("'nonce-abc123'").doesNotContain("'unsafe-inline'");
        assertThat(policy.withNonce("abc123")).doesNotContain(ContentSecurityPolicy.NONCE);
    }

    @Test
    @DisplayName("lets the SPARQL page query the Fuseki endpoint's origin, not the whole web")
    void connectsToTheSparqlOrigin() {
        assertThat(directives("n").get("connect-src"))
            .startsWith("'self' https://sparql.example.org ")
            .doesNotContain(" https: ");
    }

    @Test
    @DisplayName("lets Google Analytics run once a visitor accepts analytics cookies")
    void allowsGoogleAnalytics() {
        // Tag Manager and gtag.js, then GA4's collection endpoints (dri-one #454: staging reported
        // all four as blocked)
        assertThat(directives("n").get("script-src")).contains("https://www.googletagmanager.com");
        assertThat(directives("n").get("connect-src")).contains(
            "https://*.google-analytics.com", "https://*.analytics.google.com", "https://*.googletagmanager.com");
    }

    @Test
    @DisplayName("refuses plugins, foreign base URLs and framing by other sites, and says where to report")
    void lockedDownDefaults() {
        Map<String, String> directives = directives("n");

        assertThat(directives)
            .containsEntry("default-src", "'self'")
            .containsEntry("object-src", "'none'")
            .containsEntry("base-uri", "'self'")
            .containsEntry("frame-ancestors", "'none'")
            .containsEntry("report-uri", "/csp-reports")
            .containsEntry("report-to", ContentSecurityPolicyFilter.REPORT_GROUP);
    }

    @Test
    @DisplayName("allows images over https only, so mixed content is still reported")
    void imagesOverHttpsOnly() {
        assertThat(directives("n").get("img-src")).isEqualTo("'self' data: https:");
    }
}
