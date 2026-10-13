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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The Content-Security-Policy through the real server and security chain (dri-one #454): every response
 * carries it, inline scripts carry the same nonce as their response's policy, and anonymous browsers
 * can report violations.
 */
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("Content-Security-Policy, end to end")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContentSecurityPolicyEndToEndTest {

    private static final Pattern POLICY_NONCE = Pattern.compile("'nonce-([^']+)'");
    private static final Pattern SCRIPT_NONCE = Pattern.compile("<script nonce=\"([^\"]+)\">");

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    @SneakyThrows
    @DisplayName("an inline script carries the nonce its response's policy allows, new on every request")
    void inlineScriptCarriesTheResponseNonce() {
        var first = send(HttpRequest.newBuilder(uri("/sparql")).GET());
        var second = send(HttpRequest.newBuilder(uri("/sparql")).GET());

        assertThat(first.statusCode()).isEqualTo(200);
        String nonce = find(POLICY_NONCE, policy(first));
        assertThat(find(SCRIPT_NONCE, first.body())).isEqualTo(nonce);
        assertThat(find(POLICY_NONCE, policy(second))).isNotEqualTo(nonce);
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

        assertThat(response.statusCode()).isEqualTo(204);
    }

    @Test
    @SneakyThrows
    @DisplayName("sends the policy on responses the security chain refuses, too")
    void refusedResponsesCarryThePolicy() {
        // Every other anonymous POST needs authentication
        var response = send(HttpRequest.newBuilder(uri("/documents")).POST(HttpRequest.BodyPublishers.noBody()));

        assertThat(response.statusCode()).isIn(401, 403);
        assertThat(policy(response)).contains("default-src 'self'");
        assertThat(response.headers().firstValue("Content-Security-Policy")).isEmpty();
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
        assertThat(matcher.find()).as("%s in %s", pattern, text.length() > 300 ? text.substring(0, 300) : text).isTrue();
        return matcher.group(1);
    }
}
