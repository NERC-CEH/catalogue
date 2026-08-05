package uk.ac.ceh.gateway.catalogue.maintenance;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.indexing.jena.JenaIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.mapserver.MapServerIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndexingService;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.ADMIN;

/**
 * The regression test for the bug this feature broke on during development, and which
 * {@code AdminDeleteControllerTest} explicitly documents it cannot cover: that the CSRF token survives
 * from the GET form to the POST.
 *
 * <p>MockMvc can't exercise this — {@code SecurityMockMvcRequestPostProcessors.csrf()} swaps the live
 * {@code CsrfFilter}'s repository for a session-backed test one, so any assertion made through it is about
 * that test repository, not the real {@code CookieCsrfTokenRepository} wired up in {@code SecurityConfig}.
 * This drives real HTTP requests, with a real cookie jar, against a real embedded server instead — the
 * {@code java.net.http.HttpClient} built-in {@code CookieManager} needs no extra test dependency to do it.
 *
 * <p>Authentication here is via the real {@code Remote-User} header (as {@code RequestHeaderAuthenticationFilter}
 * expects), not {@code @WithMockCatalogueUser} — that annotation only populates the security context for the
 * thread running an in-process MockMvc call, and has no effect on requests handled by the embedded server's
 * own threads over a real socket.</p>
 */
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("AdminDeleteController CSRF, end to end")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminDeleteCsrfEndToEndTest {

    private static final Pattern CSRF_TOKEN_PATTERN = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");

    @LocalServerPort
    private int port;

    @MockitoBean CachedDataRepository cachedDataRepository;
    @MockitoBean DocumentInfoMapper<MetadataInfo> metadataInfoMapper;
    @MockitoBean DocumentTypeLookupService documentTypeLookupService;
    @MockitoBean DocumentRepository documentRepository;

    // Needed by the wider context and by the maintenance page templates
    @MockitoBean DataRepositoryOptimizingService repoService;
    @MockitoBean @Qualifier("solr-index") SolrIndexingService indexService;
    @MockitoBean @Qualifier("jena-index") JenaIndexingService linkingService;
    @MockitoBean @Qualifier("mapserver-index") MapServerIndexingService mapserverService;
    @MockitoBean CatalogueService catalogueService;
    @MockitoBean ProfileService profileService;

    @Autowired private Configuration configuration;

    private HttpClient client;

    @SneakyThrows
    @BeforeEach
    void givenFreemarkerCatalogueAndACookieJar() {
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("profile", profileService);
        given(catalogueService.defaultCatalogue()).willReturn(
            Catalogue.builder().id("eidc").title("Env Data Centre").url("https://example.com")
                .contactUrl("").logo("eidc.png").build());

        // The default HttpClient has no CookieHandler and so silently drops Set-Cookie — this is what
        // makes it a stand-in for a browser's cookie jar.
        client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    }

    @Test
    @SneakyThrows
    @DisplayName("the CSRF token from the GET form is still valid for the POST after another authenticated request")
    void tokenSurvivesAnInterveningAuthenticatedRequest() {
        var formResponse = get("/maintenance/documents/delete");
        assertThat(formResponse.statusCode(), is(200));
        var token = extractCsrfToken(formResponse.body());

        // Stands in for the browser fetching the page's stylesheet and logo before the operator submits
        // the form — an ordinary authenticated GET, on the same STATELESS-so-every-request-authenticates
        // filter chain, that doesn't itself read or regenerate a CSRF token. Under the default
        // CsrfAuthenticationStrategy this is exactly what deleted the token the first time the feature
        // broke; SecurityConfig's NullAuthenticatedSessionStrategy is what should stop that happening now.
        var interveningResponse = get("/maintenance");
        assertThat(interveningResponse.statusCode(), is(200));

        var postResponse = post(
            "/maintenance/documents/delete/preview",
            "location=METADATA_RECORD&id=35fca77f-89ce-4c40-b581-45ed039936a4&_csrf=" + token);

        assertThat(postResponse.statusCode(), is(200));
        assertThat(postResponse.body(), not(containsString("Forbidden")));
    }

    @Test
    @SneakyThrows
    @DisplayName("the POST is refused when no token is sent at all")
    void refusesWithNoTokenAtAll() {
        get("/maintenance/documents/delete"); // establishes the session's cookie jar, same as the real flow

        var postResponse = post(
            "/maintenance/documents/delete/preview",
            "location=METADATA_RECORD&id=35fca77f-89ce-4c40-b581-45ed039936a4");

        assertThat(postResponse.statusCode(), is(403));
    }

    private String extractCsrfToken(String html) {
        Matcher matcher = CSRF_TOKEN_PATTERN.matcher(html);
        if (!matcher.find()) {
            throw new AssertionError("No CSRF token field found in the rendered form:\n" + html);
        }
        return matcher.group(1);
    }

    private HttpResponse<String> get(String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(baseUrl() + path))
            .header("Remote-User", ADMIN)
            .GET()
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String formBody) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(baseUrl() + path))
            .header("Remote-User", ADMIN)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formBody))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
