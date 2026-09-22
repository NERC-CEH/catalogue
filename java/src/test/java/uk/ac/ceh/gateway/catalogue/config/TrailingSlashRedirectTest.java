package uk.ac.ceh.gateway.catalogue.config;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import java.net.URI;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression tests for the trailing-slash redirect (dri-one #292).
 *
 * <p>Before the fix, a trailing slash produced a bare 404 because
 * {@code PathPatternParser} treats {@code /documents/} and {@code /documents} as
 * distinct patterns. {@code AbstractMvcTest} wires every registered filter bean
 * into MockMvc, so the redirect filter is genuinely exercised here.
 */
@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("Trailing slash redirect")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class TrailingSlashRedirectTest extends AbstractMvcTest {

    @Test
    @SneakyThrows
    @DisplayName("redirects a trailing slash to the canonical path")
    void redirectsTrailingSlashToCanonicalPath() {
        mvc.perform(get("/documents/"))
            .andExpect(status().isPermanentRedirect())
            .andExpect(header().string("Location", "/documents"));
    }

    @Test
    @SneakyThrows
    @DisplayName("preserves the query string when redirecting")
    void preservesQueryString() {
        mvc.perform(get("/documents/?term=nitrogen&page=2&sortField=title"))
            .andExpect(status().isPermanentRedirect())
            .andExpect(header().string("Location", "/documents?term=nitrogen&page=2&sortField=title"));
    }

    @Test
    @SneakyThrows
    @DisplayName("preserves a percent-encoded facet filter verbatim")
    void preservesEncodedFacetFilter() {
        // The URI overload is used deliberately: get(String) re-encodes the template, which would
        // turn %7C into %257C and test the harness rather than the filter. Facet filters arrive
        // percent-encoded ("field|value"), so they must survive the redirect untouched.
        mvc.perform(get(URI.create("/documents/?facet=topic%7CHydrology")))
            .andExpect(status().isPermanentRedirect())
            .andExpect(header().string("Location", "/documents?facet=topic%7CHydrology"));
    }

    @Test
    @SneakyThrows
    @DisplayName("redirects catalogue-scoped search paths")
    void redirectsCatalogueScopedPath() {
        mvc.perform(get("/eidc/documents/"))
            .andExpect(status().isPermanentRedirect())
            .andExpect(header().string("Location", "/eidc/documents"));
    }

    @Test
    @SneakyThrows
    @DisplayName("uses 308 so a non-safe method survives the redirect")
    void usesPermanentRedirectStatus() {
        mvc.perform(get("/documents/"))
            .andExpect(status().is(308));
    }

    @Test
    @SneakyThrows
    @DisplayName("leaves the site root alone")
    void leavesSiteRootAlone() {
        mvc.perform(get("/"))
            .andExpect(status().is(not(308)));
    }

    @Test
    @SneakyThrows
    @DisplayName("redirects the WAF listing to its canonical slash-less path")
    void redirectsWafListing() {
        mvc.perform(get("/documents/gemini/waf/"))
            .andExpect(status().isPermanentRedirect())
            .andExpect(header().string("Location", "/documents/gemini/waf"));
    }
}
