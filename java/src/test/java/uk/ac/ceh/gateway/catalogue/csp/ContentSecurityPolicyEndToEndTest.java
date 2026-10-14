package uk.ac.ceh.gateway.catalogue.csp;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.oneOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Content-Security-Policy through the real server and security chain (dri-one #454). Every response
 * carries it, with a fresh nonce for any inline script. The SPARQL page, which had the only inline
 * script, now has none (dri-one #367). Anonymous browsers can report violations.
 */
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("Content-Security-Policy, end to end")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContentSecurityPolicyEndToEndTest {

    private static final Pattern POLICY_NONCE = Pattern.compile("'nonce-([^']+)'");
    private static final Pattern INLINE_SCRIPT = Pattern.compile("<script(?![^>]*\\bsrc=)[^>]*>");

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    @SneakyThrows
    @DisplayName("the SPARQL page runs no inline script, and each response's policy has a nonce of its own")
    void noInlineScriptAndAFreshNonce() {
        var first = send(HttpRequest.newBuilder(uri("/sparql")).GET());
        var second = send(HttpRequest.newBuilder(uri("/sparql")).GET());

        assertThat(first.statusCode(), equalTo(200));
        // Its start-up script is bundled now, and the page hands it the endpoint as data (dri-one #367)
        assertThat(first.body(), containsString("data-endpoint="));
        assertFalse(INLINE_SCRIPT.matcher(first.body()).find(), "the SPARQL page has an inline script");
        assertThat(find(POLICY_NONCE, policy(first)), not(equalTo(find(POLICY_NONCE, policy(second)))));
    }

    @Test
    @SneakyThrows
    @DisplayName("lets an anonymous browser report a violation")
    void anonymousReportsAreAccepted() {
        var response = send(HttpRequest.newBuilder(uri(CspReportController.PATH))
            .header("Content-Type", "application/csp-report")
            .POST(HttpRequest.BodyPublishers.ofString(
                "{\"csp-report\": {\"effective-directive\": \"img-src\", \"blocked-uri\": \"http://a.example/i.png\", "
                    + "\"document-uri\": \"https://catalogue.example/\"}}")));

        assertThat(response.statusCode(), equalTo(204));
    }

    @Test
    @SneakyThrows
    @DisplayName("sends the policy on responses the security chain refuses, too")
    void refusedResponsesCarryThePolicy() {
        // Every other anonymous POST needs authentication
        var response = send(HttpRequest.newBuilder(uri("/documents")).POST(HttpRequest.BodyPublishers.noBody()));

        assertThat(response.statusCode(), oneOf(401, 403));
        assertThat(policy(response), containsString("default-src 'self'"));
        assertFalse(response.headers().firstValue("Content-Security-Policy").isPresent(), "an enforced policy was sent");
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    @SneakyThrows
    private HttpResponse<String> send(HttpRequest.Builder request) {
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String policy(HttpResponse<String> response) {
        return response.headers().firstValue(ContentSecurityPolicyFilter.HEADER).orElse("");
    }

    private static String find(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        assertTrue(matcher.find(), () -> String.format("%s in %s", pattern, text.length() > 300 ? text.substring(0, 300) : text));
        return matcher.group(1);
    }
}
