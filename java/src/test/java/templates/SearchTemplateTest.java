package templates;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.userdetails.SecurityUserInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;
import uk.ac.ceh.gateway.catalogue.search.SearchQuery;
import uk.ac.ceh.gateway.catalogue.search.SearchResults;
import uk.ac.ceh.gateway.catalogue.search.SpatialOperation;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Renders {@code html/search.ftlh}, whose search form is half of a contract with the
 * Backbone view in {@code web/src/search/src/views/SearchFormView.js}. That view finds
 * the live term control with {@code [name='term']:not(:disabled)}, listens for the
 * form's {@code submit} event, and swaps the single line input for the multi row
 * textarea when semantic search is switched on. None of that is expressible in the
 * template, and a rename or an attribute dropped here breaks the search box with no
 * Java-side failure at all - the Karma suite would still pass, because it builds its
 * own fixture markup rather than reading this file.
 * <p>
 * The template is rendered whole rather than through a wrapper: the form is inline in
 * {@code search.ftlh}, so there is no fragment to include on its own. That pulls in
 * {@code skeleton.ftlh} and the two search includes, hence the shared variables below.
 */
class SearchTemplateTest {
    private static final String TEMPLATE = "html/search.ftlh";

    /** SearchFormView reads the live control with [name='term']:not(:disabled). */
    private static final String TERM_ATTRIBUTE = "name=\"term\"";

    Configuration configuration;

    @SneakyThrows
    @BeforeEach
    void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setTemplateLoader(new FileTemplateLoader(new File("../templates")));
        // Production sets spring.freemarker.settings.template_exception_handler=rethrow.
        // FreeMarker's default handler writes the error into the output instead, which
        // would let a broken template still satisfy a "contains" assertion.
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        // Also from application.properties. functions.ftlh's temporaryBlock macro parses
        // its expiry with ?date, which needs this; without it the whole page throws.
        configuration.setDateFormat("yyyy-MM-dd");
        configuration.setSharedVariable("catalogues", catalogues());
        configuration.setSharedVariable("codes", mock(CodeLookupService.class));
        configuration.setSharedVariable("permission", mock(PermissionService.class));
        configuration.setSharedVariable("profile", mock(ProfileService.class));
        configuration.setSharedVariable("userInfo", mock(SecurityUserInfo.class));
    }

    private Catalogue eidc() {
        return Catalogue.builder()
            .id("eidc")
            .title("Environmental Information Data Centre")
            .url("https://catalogue.ceh.ac.uk")
            .contactUrl("https://catalogue.ceh.ac.uk/contact")
            .logo("UKCEH_EIDC_black.png")
            .build();
    }

    private CatalogueService catalogues() {
        val catalogues = mock(CatalogueService.class);
        when(catalogues.retrieve(anyString())).thenReturn(eidc());
        when(catalogues.defaultCatalogue()).thenReturn(eidc());
        return catalogues;
    }

    @SuppressWarnings("unchecked")
    private SearchResults results(boolean semanticEnabled) {
        val query = new SearchQuery(
            "https://catalogue.ceh.ac.uk/documents",
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            null,
            SpatialOperation.ISWITHIN,
            1,
            20,
            Collections.emptyList(),
            mock(GroupStore.class),
            eidc(),
            Collections.emptyList(),
            null,
            SolrQuery.ORDER.desc
        );
        val basic = new SearchResults(mock(QueryResponse.class), query, List.of());
        return semanticEnabled ? new SearchResults(basic, true) : basic;
    }

    @SneakyThrows
    private String render(boolean semanticEnabled) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate(TEMPLATE),
            results(semanticEnabled)
        );
    }

    /** The opening tag of the first element of the given name, so attributes can be asserted on. */
    private String openingTag(String html, String element) {
        Matcher matcher = Pattern.compile("<" + element + "\\b[^>]*>").matcher(html);
        assertThat(matcher.find()).as("no <%s> in the rendered page", element).isTrue();
        return matcher.group();
    }

    private int occurrences(String html, String needle) {
        return html.split(Pattern.quote(needle), -1).length - 1;
    }

    // ---------------------------------------------------------------- keyword mode

    /**
     * Semantic search is off for every catalogue that has not enabled it, and those
     * pages must be exactly as they were: one single line term input, no textarea and
     * no toggle. This is the "returns to the previous behaviour and display" half of
     * the requirement, pinned at the markup level.
     */
    @Test
    void withoutSemanticSearchOnlyTheSingleLineInputIsRendered() {
        //when
        val actual = render(false);

        //then
        assertThat(actual).contains("<input placeholder=\"Search…\" " + TERM_ATTRIBUTE);
        assertThat(occurrences(actual, TERM_ATTRIBUTE)).isEqualTo(1);
        assertThat(actual).doesNotContain("<textarea");
        assertThat(actual).doesNotContain("name=\"semantic\"");
        assertThat(actual).doesNotContain("search-button-label");
    }

    // --------------------------------------------------------------- semantic mode

    /**
     * The textarea is what makes a natural language query typeable. Its {@code rows}
     * is the whole point of it being a textarea rather than an input, so pin that it
     * is more than one.
     */
    @Test
    void withSemanticSearchAMultiRowTextareaIsRendered() {
        //when
        val actual = render(true);

        //then
        val textarea = openingTag(actual, "textarea");
        assertThat(textarea).contains(TERM_ATTRIBUTE);
        assertThat(Integer.parseInt(
            openingTag(actual, "textarea").replaceAll(".*\\brows=\"(\\d+)\".*", "$1")
        )).isGreaterThan(1);
    }

    /**
     * Both controls are served, because SearchFormView swaps between them client side
     * rather than re-rendering. The one that is not in use has to arrive disabled: that
     * is the only thing telling {@code [name='term']:not(:disabled)} which is live, and
     * it is also what keeps the no-JS {@code GET /documents} fallback sending a single
     * {@code term} parameter instead of two.
     */
    @Test
    void theTextareaIsServedDisabledSoOnlyOneTermControlIsLive() {
        //when
        val actual = render(true);

        //then
        assertThat(occurrences(actual, TERM_ATTRIBUTE)).isEqualTo(2);
        assertThat(openingTag(actual, "textarea")).contains("disabled");
        assertThat(openingTag(actual, "input")).doesNotContain("disabled");
    }

    /**
     * Hiding the inactive control needs Bootstrap's {@code d-none} rather than the
     * {@code hidden} attribute: {@code [hidden]}'s {@code display: none} comes from the
     * user agent stylesheet, so {@code .form-control}'s {@code display: block} beats it
     * and the textarea would show in both modes.
     */
    @Test
    void theTextareaIsServedHiddenWithTheBootstrapUtilityClass() {
        //when
        val actual = render(true);

        //then
        assertThat(openingTag(actual, "textarea")).contains("d-none");
    }

    @Test
    void withSemanticSearchTheToggleAndButtonLabelAreRendered() {
        //when
        val actual = render(true);

        //then
        assertThat(actual)
            .contains("name=\"semantic\"")
            .contains("id=\"semanticSearch\"")
            .contains("Semantic search")
            .contains("search-button-label");
    }

    // ------------------------------------------------------------- both modes

    /**
     * In semantic mode the button is the only way to start a search - the term is
     * deliberately withheld from the model while typing, so every keystroke does not
     * cost a Bedrock call to embed the query. SearchFormView hangs that off the form's
     * {@code submit} event, so a button that is not a submit button silently stops
     * semantic search working. It also has to stay keyboard reachable: it used to carry
     * {@code tabindex="-1"} because it did nothing at all.
     */
    @Test
    void theSearchButtonSubmitsTheFormAndIsKeyboardReachable() {
        for (boolean semanticEnabled : List.of(false, true)) {
            //when
            val actual = render(semanticEnabled);

            //then
            val button = openingTag(actual, "button");
            assertThat(button).as("semanticEnabled=%s", semanticEnabled)
                .contains("type=\"submit\"")
                .doesNotContain("tabindex=\"-1\"");
        }
    }

    /**
     * The form view is bound to {@code .search-form} and toggles {@code semantic-mode}
     * and {@code term-pending} on it, and the results view to {@code .results}. Those
     * are the selectors {@code SearchAppView.render()} passes as each sub view's
     * {@code el}; a view given an empty selection fails silently.
     */
    @Test
    void theHooksTheJavascriptViewsBindToArePresent() {
        //when
        val actual = render(true);

        //then
        assertThat(actual)
            .contains("class=\"search-form\"")
            .contains("class=\"results\"")
            .contains("class=\"facet-filter\"")
            .contains("class=\"mapsearch\"");
    }
}
