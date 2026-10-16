package uk.ac.ceh.gateway.catalogue.csp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

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

        assertThat(scripts, allOf(containsString("'nonce-abc123'"), not(containsString("'unsafe-inline'"))));
        assertThat(policy.withNonce("abc123"), not(containsString(ContentSecurityPolicy.NONCE)));
    }

    @Test
    @DisplayName("refuses eval: the front-end templates are compiled at build time")
    void noEval() {
        // dri-one #464: underscore's _.template compiled them in the browser with new Function
        assertThat(directives("n").get("script-src"), not(containsString("'unsafe-eval'")));
    }

    @Test
    @DisplayName("lets the SPARQL page query the Fuseki endpoint's origin, not the whole web")
    void connectsToTheSparqlOrigin() {
        assertThat(directives("n").get("connect-src"), allOf(
            startsWith("'self' https://sparql.example.org "),
            not(containsString(" https: "))));
    }

    @Test
    @DisplayName("lets Google Analytics run once a visitor accepts analytics cookies")
    void allowsGoogleAnalytics() {
        // Tag Manager and gtag.js, then GA4's collection endpoints (dri-one #454: staging reported
        // all four as blocked)
        assertThat(directives("n").get("script-src"), containsString("https://www.googletagmanager.com"));
        assertThat(directives("n").get("connect-src"), allOf(
            containsString("https://*.google-analytics.com"),
            containsString("https://*.analytics.google.com"),
            containsString("https://*.googletagmanager.com")));
    }

    @Test
    @DisplayName("serves YASGUI and the Inter font itself, so neither unpkg nor Google Fonts is allowed")
    void noUnpkgOrGoogleFonts() {
        // Bundled and self-hosted since dri-one #367
        Map<String, String> directives = directives("n");

        assertThat(directives.get("script-src"), not(containsString("unpkg.com")));
        assertThat(directives.get("style-src"), allOf(not(containsString("unpkg.com")), not(containsString("fonts.googleapis.com"))));
        assertThat(directives.get("font-src"), equalTo("'self' data:"));
    }

    @Test
    @DisplayName("no longer allows the Jira issue collector's scripts or styles")
    void noJiraIssueCollector() {
        // Removed: jira.ceh.ac.uk won't let its form be framed by another site (dri-one #477)
        Map<String, String> directives = directives("n");

        assertThat(directives.get("script-src"), not(containsString("jira.ceh.ac.uk")));
        assertThat(directives.get("style-src"), not(containsString("jira.ceh.ac.uk")));
    }

    @Test
    @DisplayName("refuses plugins, foreign base URLs and framing by other sites, and says where to report")
    void lockedDownDefaults() {
        Map<String, String> directives = directives("n");

        assertThat(directives, allOf(
            hasEntry("default-src", "'self'"),
            hasEntry("object-src", "'none'"),
            hasEntry("base-uri", "'self'"),
            hasEntry("frame-ancestors", "'none'"),
            hasEntry("report-uri", "/csp-reports"),
            hasEntry("report-to", ContentSecurityPolicyFilter.REPORT_GROUP)));
    }

    @Test
    @DisplayName("allows images over https only, so mixed content is still reported")
    void imagesOverHttpsOnly() {
        assertThat(directives("n").get("img-src"), equalTo("'self' data: https:"));
    }
}
