package uk.ac.ceh.gateway.catalogue.depositRequest;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.ADMIN;

/**
 * The regression test for the JIRA reference number vanishing from the deposit request success page.
 *
 * <p>The reference number used to be stashed in the {@code HttpSession} by the POST and read back out
 * by the success page on a separate request. Behind the reverse proxy the browser ended up holding
 * more than one session cookie — the proxy scopes each rewritten cookie to the exact request URI
 * rather than to {@code /}, so a cookie pinned at {@code Path=/deposit-request/success} outranked the
 * one the submission set at {@code Path=/deposit-request}, and the container honoured the first
 * session id it was sent. The JIRA issue was raised correctly and the page still rendered; only the
 * reference was missing.</p>
 *
 * <p>The reference now travels in the redirect, so there is nothing for a session to lose. That is
 * what {@link #referenceNumberSurvivesAStaleSessionCookie()} pins down, and it is the assertion that
 * fails on the old implementation.</p>
 *
 * <p>Note that authenticated requests here still open a session, which is nothing to do with this
 * flow: {@code AbstractPreAuthenticatedProcessingFilter} defaults to an
 * {@code HttpSessionSecurityContextRepository}, and the hand-built {@code RequestHeaderAuthenticationFilter}
 * added via {@code HttpSecurity.addFilter} never receives the repository that
 * {@code SessionCreationPolicy.STATELESS} configures. These tests therefore assert that the reference
 * survives regardless of what session the browser presents, rather than that no session exists.</p>
 *
 * <p>This drives real HTTP against a real embedded server with a real cookie jar, as
 * {@code AdminDeleteCsrfEndToEndTest} does — MockMvc cannot see cookie behaviour. Authentication is
 * via the real {@code Remote-User} header rather than {@code @WithMockCatalogueUser}, which has no
 * effect on requests handled by the embedded server's own threads over a real socket.</p>
 */
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("Deposit request success page, end to end")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DepositRequestSuccessEndToEndTest {

    private static final DepositRequestModel VALID_REQUEST = new DepositRequestModel(
        "Alice Smith", "alice@example.com", "UKCEH", "2025_Biodiversity and Land Use", true, true, true, true,
        "NERC", "", "NE123", "Yes", "Model",
        true, true, false, false,
        List.of(new DataResourceModel(
            "Title", "Description", "Images", "", true, "NetCDF", "",
            "1000", false)),
        "Some notes");

    @LocalServerPort
    private int port;

    @Autowired
    private JsonMapper mapper;

    @MockitoBean
    private DepositRequestService service;

    private HttpClient client;

    @BeforeEach
    void givenAJiraIssueIsCreatedAndACookieJar() {
        ObjectNode jiraResponse = JsonMapper.builder().build().createObjectNode();
        jiraResponse.put("key", "TEST-123");
        jiraResponse.put("componentName", DepositRequestService.INGESTION_MANAGEMENT_COMPONENT);
        given(service.handleSubmission(any(DepositRequestModel.class))).willReturn(jiraResponse);

        // The default HttpClient has no CookieHandler and so silently drops Set-Cookie — this is what
        // makes it a stand-in for a browser's cookie jar.
        client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    }

    @Test
    @SneakyThrows
    @DisplayName("the success page shows the JIRA reference number from the submission that led to it")
    void referenceNumberSurvivesFromSubmissionToSuccessPage() {
        var submission = post("/deposit-request", mapper.writeValueAsString(VALID_REQUEST));
        assertThat(submission.body(), submission.statusCode(), is(201));

        var success = get(submission.headers().firstValue("Location").orElseThrow());

        assertThat(success.statusCode(), is(200));
        assertThat(success.body(), containsString("TEST-123"));
    }

    /**
     * This is the assertion that fails on the old, session-backed implementation, and it reproduces the
     * reported bug directly: behind the proxy the success request arrived carrying a stale session
     * cookie left over from an earlier visit, so the reference number written by the submission was
     * never found. Carrying it in the redirect makes the request self-contained — whatever session the
     * browser happens to present is irrelevant.
     */
    @Test
    @SneakyThrows
    @DisplayName("the success page shows the reference even when the browser sends a stale session")
    void referenceNumberSurvivesAStaleSessionCookie() {
        var staleSession = staleSessionCookie();

        var submission = post("/deposit-request", mapper.writeValueAsString(VALID_REQUEST));
        var location = submission.headers().firstValue("Location").orElseThrow();

        var request = HttpRequest.newBuilder(URI.create(baseUrl() + location))
            .header("Remote-User", ADMIN)
            .header("Cookie", staleSession)
            .GET()
            .build();
        // A bare client, so its cookie jar cannot add the submission's own session to the header
        var success = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(success.statusCode(), is(200));
        assertThat(success.body(), containsString("TEST-123"));
    }

    @Test
    @SneakyThrows
    @DisplayName("the success page shows the reference to a browser holding no session at all")
    void referenceNumberNeedsNoSession() {
        var submission = post("/deposit-request", mapper.writeValueAsString(VALID_REQUEST));
        var location = submission.headers().firstValue("Location").orElseThrow();

        var request = HttpRequest.newBuilder(URI.create(baseUrl() + location))
            .header("Remote-User", ADMIN)
            .GET()
            .build();
        var success = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(success.body(), containsString("TEST-123"));
    }

    /**
     * A session belonging to some earlier, unrelated request, exactly as a browser would still be
     * holding one from a previous visit to this page.
     */
    private String staleSessionCookie() throws Exception {
        return get("/deposit-request/success").headers().allValues("Set-Cookie").stream()
            .filter(header -> header.startsWith("JSESSIONID="))
            .map(header -> header.split(";")[0])
            .findFirst()
            .orElseThrow(() -> new AssertionError("expected an earlier request to have opened a session"));
    }

    @Test
    @SneakyThrows
    @DisplayName("the success page still shows the reference when revisited")
    void successPageCanBeRevisited() {
        var submission = post("/deposit-request", mapper.writeValueAsString(VALID_REQUEST));
        var location = submission.headers().firstValue("Location").orElseThrow();

        assertThat(get(location).body(), containsString("TEST-123"));
        assertThat("a refresh or a bookmark must still work",
            get(location).body(), containsString("TEST-123"));
    }

    @Test
    @SneakyThrows
    @DisplayName("the success page ignores a reference number invented in the URL")
    void successPageIgnoresAnInventedReference() {
        var success = get("/deposit-request/success?reference=totally-made-up");

        assertThat(success.statusCode(), is(200));
        assertThat(success.body(), containsString("Thank you"));
        assertThat(success.body(), not(containsString("totally-made-up")));
    }

    private HttpResponse<String> get(String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(baseUrl() + path))
            .header("Remote-User", ADMIN)
            .GET()
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String jsonBody) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(baseUrl() + path))
            .header("Remote-User", ADMIN)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
