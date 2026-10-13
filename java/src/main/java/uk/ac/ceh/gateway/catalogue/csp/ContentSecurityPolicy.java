package uk.ac.ceh.gateway.catalogue.csp;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The catalogue's Content-Security-Policy (NERC-CEH/dri-one#454), sent in report-only mode by
 * {@link ContentSecurityPolicyFilter} while the violations it reports are worked through.
 *
 * <p>Why each source is allowed:</p>
 * <ul>
 *   <li>{@code script-src}: the bundle is our own. Inline scripts need the per-request nonce
 *   ({@code <script nonce="${csp.nonce}">}). {@code 'unsafe-eval'} is there because underscore's
 *   {@code _.template} compiles every front-end template with {@code new Function} when it loads;
 *   precompiling them at build time would let it go (dri-one #464). jira.ceh.ac.uk is the EIDC issue collector,
 *   unpkg.com the YASGUI editor on the maintenance SPARQL page (#367).</li>
 *   <li>{@code style-src}: {@code 'unsafe-inline'} because templates set {@code style} attributes and
 *   the SPARQL page has an inline stylesheet; styles can't run code.</li>
 *   <li>{@code img-src https:}: a record's browse graphic and the map tiles can come from any host.</li>
 *   <li>{@code connect-src}: the SPARQL page queries the Fuseki endpoint, which is a different host.</li>
 *   <li>{@code frame-src https:}: a dataset's spatial explorer is embedded from the URL its record
 *   gives, and the issue collector opens in a frame.</li>
 * </ul>
 */
public class ContentSecurityPolicy {

    /** Replaced by the request's nonce in {@link #withNonce(String)}. */
    static final String NONCE = "{nonce}";

    private final String template;

    /**
     * @param sparqlEndpoint the Fuseki SPARQL endpoint the SPARQL page queries from the browser
     * @param reportPath     where browsers send violation reports
     */
    public ContentSecurityPolicy(String sparqlEndpoint, String reportPath) {
        Map<String, List<String>> directives = new LinkedHashMap<>();
        directives.put("default-src", List.of("'self'"));
        directives.put("script-src", List.of("'self'", "'nonce-" + NONCE + "'", "'unsafe-eval'",
            "https://jira.ceh.ac.uk", "https://unpkg.com"));
        directives.put("style-src", List.of("'self'", "'unsafe-inline'", "https://fonts.googleapis.com",
            "https://jira.ceh.ac.uk", "https://unpkg.com"));
        directives.put("font-src", List.of("'self'", "data:", "https://fonts.gstatic.com"));
        directives.put("img-src", List.of("'self'", "data:", "https:"));
        directives.put("connect-src", List.of("'self'", origin(sparqlEndpoint)));
        directives.put("frame-src", List.of("'self'", "https:"));
        directives.put("object-src", List.of("'none'"));
        directives.put("base-uri", List.of("'self'"));
        directives.put("form-action", List.of("'self'"));
        // The same as the X-Frame-Options: DENY Spring Security already sends
        directives.put("frame-ancestors", List.of("'none'"));
        directives.put("report-uri", List.of(reportPath));
        directives.put("report-to", List.of(ContentSecurityPolicyFilter.REPORT_GROUP));
        this.template = directives.entrySet().stream()
            .map(directive -> directive.getKey() + " " + String.join(" ", directive.getValue()))
            .collect(Collectors.joining("; "));
    }

    /** The policy for one response, allowing inline scripts that carry this nonce. */
    public String withNonce(String nonce) {
        return template.replace(NONCE, nonce);
    }

    private static String origin(String url) {
        URI uri = URI.create(url);
        return uri.getScheme() + "://" + uri.getAuthority();
    }
}
