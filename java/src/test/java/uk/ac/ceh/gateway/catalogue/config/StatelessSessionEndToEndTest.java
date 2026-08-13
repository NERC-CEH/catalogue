package uk.ac.ceh.gateway.catalogue.config;

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
import uk.ac.ceh.gateway.catalogue.depositRequest.DataResourceModel;
import uk.ac.ceh.gateway.catalogue.depositRequest.DepositRequestModel;
import uk.ac.ceh.gateway.catalogue.depositRequest.DepositRequestService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.ADMIN;

/**
 * {@link SecurityConfig} declares {@code SessionCreationPolicy.STATELESS}. This holds the whole running
 * application to that claim: no request, authenticated or not, may come back with a session cookie.
 *
 * <p>{@link AuthFilterStatelessTest} pins the two authentication filters individually, which is where the
 * defect was. This test is deliberately broader and coarser — it watches the wire, so it also catches any
 * <em>other</em> component that starts creating sessions later, wherever in the stack it sits. That is
 * worth having, because the failure it guards against is silent: a session cookie costs nothing visible
 * until it reaches the reverse proxy, which scopes it to the exact request URI rather than {@code /}. The
 * browser then stacks same-named cookies, sends the stale one first, and the container honours it. The
 * only symptom is a feature that works once per browser and never again (dri-one #271, #272; GitLab MR
 * !1155).</p>
 *
 * <p>A real embedded server over a real socket is required: {@code MockMvc} never surfaced any of this,
 * and it cannot — the session cookie is the evidence. Authentication is via the real {@code Remote-User}
 * header for the same reason, as {@code @WithMockCatalogueUser} has no effect on requests the container
 * handles on its own threads. Following {@code AdminDeleteCsrfEndToEndTest} and
 * {@code DepositRequestSuccessEndToEndTest}.</p>
 *
 * <p>{@code /deposit-request} stands in as "some authenticated write" simply because its one collaborator
 * mocks cleanly; the security chain requires full authentication for every POST, PUT and DELETE, so any
 * of them would do.</p>
 */
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("The application's declared statelessness, end to end")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StatelessSessionEndToEndTest {

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

    @BeforeEach
    void givenAJiraIssueIsCreated() {
        ObjectNode jiraResponse = JsonMapper.builder().build().createObjectNode();
        jiraResponse.put("key", "TEST-123");
        jiraResponse.put("componentName", DepositRequestService.INGESTION_MANAGEMENT_COMPONENT);
        given(service.handleSubmission(any(DepositRequestModel.class))).willReturn(jiraResponse);
    }

    /**
     * The regression assertion. Before the fix this request came back with a {@code JSESSIONID} — the
     * authentication filter saved its security context into a session nothing would ever read.
     */
    @Test
    @SneakyThrows
    @DisplayName("an authenticated page view opens no session")
    void authenticatedReadOpensNoSession() {
        var response = get("/deposit-request/success", ADMIN);

        assertThat(response.statusCode(), is(200));
        assertThat("a stateless application must not set a session cookie",
            sessionCookies(response), is(empty()));
    }

    /**
     * {@code /robots.txt} rather than the success page, because that page is
     * {@code @PreAuthorize}-guarded and an anonymous request to it is correctly refused. A genuinely
     * public page is what is wanted here.
     */
    @Test
    @SneakyThrows
    @DisplayName("an anonymous page view opens no session")
    void anonymousReadOpensNoSession() {
        var response = get("/robots.txt", null);

        assertThat(response.statusCode(), is(200));
        assertThat(sessionCookies(response), is(empty()));
    }

    @Test
    @SneakyThrows
    @DisplayName("an authenticated write opens no session")
    void authenticatedWriteOpensNoSession() {
        var response = post("/deposit-request", ADMIN);

        assertThat(response.body(), response.statusCode(), is(201));
        assertThat(sessionCookies(response), is(empty()));
    }

    /**
     * Proves the fix did not simply move the problem: authentication is genuinely re-established from the
     * {@code Remote-User} header on each request, so a client that carries nothing at all between requests
     * is authenticated every time. Each call uses a fresh, cookie-less client, which is a harsher client
     * than any browser.
     */
    @Test
    @SneakyThrows
    @DisplayName("consecutive authenticated writes both succeed with nothing carried between them")
    void authenticationIsPerRequest() {
        assertThat(post("/deposit-request", ADMIN).statusCode(), is(201));
        assertThat("the second write must not depend on state left by the first",
            post("/deposit-request", ADMIN).statusCode(), is(201));
    }

    /**
     * The control for {@link #authenticationIsPerRequest()}: without the header the write is refused, so
     * the 201s above are evidence of authentication rather than of a chain that permits everything.
     */
    @Test
    @SneakyThrows
    @DisplayName("a write with no credentials is still refused")
    void unauthenticatedWriteIsRefused() {
        assertThat(post("/deposit-request", null).statusCode(), is(403));
    }

    private List<String> sessionCookies(HttpResponse<String> response) {
        return response.headers().allValues("Set-Cookie").stream()
            .filter(header -> header.startsWith("JSESSIONID="))
            .toList();
    }

    private HttpResponse<String> get(String path, String remoteUser) throws Exception {
        return send(authenticate(HttpRequest.newBuilder(URI.create(baseUrl() + path)), remoteUser).GET());
    }

    private HttpResponse<String> post(String path, String remoteUser) throws Exception {
        var body = HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(VALID_REQUEST));
        return send(authenticate(HttpRequest.newBuilder(URI.create(baseUrl() + path)), remoteUser)
            .header("Content-Type", "application/json")
            .POST(body));
    }

    private HttpRequest.Builder authenticate(HttpRequest.Builder request, String remoteUser) {
        return remoteUser == null ? request : request.header("Remote-User", remoteUser);
    }

    /**
     * A fresh client per request, with no {@code CookieHandler} — so nothing can be carried from one
     * request to the next, and every {@code Set-Cookie} is left for the assertions to inspect.
     */
    private HttpResponse<String> send(HttpRequest.Builder request) throws Exception {
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
