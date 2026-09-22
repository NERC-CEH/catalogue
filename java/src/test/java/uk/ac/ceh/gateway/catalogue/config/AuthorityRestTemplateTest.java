package uk.ac.ceh.gateway.catalogue.config;

import com.sun.net.httpserver.HttpServer;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Exercises the real {@code authorities} RestTemplate against a real socket.
 *
 * <p>Every other test of the retrievers uses {@code MockRestServiceServer},
 * which <em>replaces</em> the request factory — so the timeouts and the
 * redirect policy configured on this bean were never executed by any test at
 * all. That gap let a total failure of the ORCID half of dri-one #350 phase 3
 * through a green suite: the JDK's HttpClient does not follow redirects by
 * default, and both ORCID and AGROVOC redirect.
 */
@DisplayName("The RestTemplate used for third-party authorities")
class AuthorityRestTemplateTest {

    private HttpServer server;
    private RestTemplate restTemplate;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        // Shaped like ORCID's: a redirect to another path, whose body is short
        // HTML rather than the RDF that was asked for.
        server.createContext("/redirecting", exchange -> {
            exchange.getResponseHeaders().add("Location", "/target");
            val body = "<html><body>Moved</body></html>".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(302, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/target", exchange -> {
            val body = "<urn:x> a <urn:y> .".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        restTemplate = new ServicesConfig()
            .authorityRestTemplate(Duration.ofSeconds(2), Duration.ofSeconds(2));
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    @DisplayName("follows a redirect, because both ORCID and AGROVOC serve their RDF behind one")
    void followsRedirects() {
        val body = restTemplate.getForObject(baseUrl + "/redirecting", String.class);

        assertThat(
            "an unfollowed redirect hands back its own HTML body, which is not blank, "
                + "so it reaches the RDF parser and fails there instead",
            body, is("<urn:x> a <urn:y> .")
        );
    }
}
