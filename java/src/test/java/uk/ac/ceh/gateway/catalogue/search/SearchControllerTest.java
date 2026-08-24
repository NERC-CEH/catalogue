package uk.ac.ceh.gateway.catalogue.search;

import freemarker.template.Configuration;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndex;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.UNPRIVILEGED_USERNAME;

@WithMockCatalogueUser
@Slf4j
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("SearchController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})

@TestPropertySource(locations="classpath:test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SearchControllerTest extends AbstractMvcTest {
    @MockitoBean private SolrClient solrClient;
    @MockitoBean private CatalogueService catalogueService;
    @MockitoBean private FacetFactory facetFactory;
    @MockitoBean private CodeLookupService codeLookupService;
    @MockitoBean(name="permission") private PermissionService permissionService;
    @MockitoBean private ProfileService profileService;
    @MockitoBean private Searcher searcher;
    @Autowired Configuration configuration;

    private final String catalogueKey = "eidc";
    private final String editorDropdownOpeningDiv = "<div id=\"editorCreate\" class=\"dropdown\">";
    private final Catalogue eidc = Catalogue.builder()
        .id(catalogueKey)
        .title("Env Data Centre")
        .url("https://example.com")
        .contactUrl("")
        .logo("eidc.png")
        .build();
    private final Catalogue allCatalogues = Catalogue.builder()
        .id("all")
        .title("All catalogues")
        .url("")
        .contactUrl("")
        .logo("ukceh.png")
        .build();

    private void givenCatalogue() {
        given(catalogueService.retrieve(catalogueKey))
            .willReturn(
                Catalogue.builder()
                    .id(catalogueKey)
                    .title("Env Data Centre")
                    .url("https://example.com")
                    .contactUrl("")
                    .logo("eidc.png")
                    .build()
            );
    }

    @SneakyThrows
    private void givenSearchResultsForAllCatalogues() {
        val endpoint = "http://localhost/documents";
        val term = "carbon";
        val results = Arrays.asList(create("0"), create("1"));
        val searchResults = new SearchResults(
            20,
            term,
            1,
            20,
            endpoint,
            null,
            null,
            null,
            null,
            null,
            results,
            Collections.emptyList(),
            allCatalogues,
            Collections.emptyList(),
            null,
            "asc"
        );
        given(searcher.search(
            any(), any(), any(), any(), any(), anyInt(), anyInt(), any(),
            eq("all"),
            any(), any()
        )).willReturn(searchResults);

        given(codeLookupService.lookup("publication.state", "public")).willReturn("Public");
    }

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("codes", codeLookupService);
        configuration.setSharedVariable("permission", permissionService);
        configuration.setSharedVariable("profile", profileService);
    }

    private void givenUserCanCreate() {
        given(permissionService.userCanCreate(catalogueKey))
            .willReturn(true);
    }

    @SneakyThrows
    private void givenSearchResults() {
        val endpoint = "http://localhost/eidc/documents";
        val term = "carbon";
        val results = Arrays.asList(
            create("0"),
            create("1")
        );
        val relatedSearches = List.of(Link.builder().href("https://example.com/related").title("related").build());
        val searchResults = new SearchResults(
            20,
            term,
            1,
            20,
            endpoint,
            "without",
            "intersecting",
            "within",
            "prev",
            "next",
            results,
            Collections.emptyList(),
            eidc,
            relatedSearches,
            "publicationDate",
            "desc"
        );
        given(searcher.search(
            any(),
            any(),
            any(),
            any(),
            any(),
            anyInt(),
            anyInt(),
            any(),
            any(),
            any(),
            any()
        )).willReturn(searchResults);

        given(codeLookupService.lookup("publication.state", "public")).willReturn("Public");
    }

    private SolrIndex create(String id) {
        val solrIndex = new SolrIndex();
        solrIndex.setIdentifier(id);
        solrIndex.setTitle("title-" + id);
        solrIndex.setState("public");
        return solrIndex;
    }

    @Test
    @DisplayName("GET /documents returns unified search results as HTML")
    @SneakyThrows
    void getUnifiedSearchPageHtml() {
        //given
        givenSearchResultsForAllCatalogues();
        givenFreemarkerConfiguration();

        //when
        mvc.perform(
            get("/documents")
                .queryParam("term", "carbon")
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_HTML));
    }

    @Test
    @DisplayName("GET /documents returns unified search results as JSON")
    @SneakyThrows
    void getUnifiedSearchResultsJson() {
        //given
        givenSearchResultsForAllCatalogues();

        //when
        mvc.perform(
            get("/documents")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @SneakyThrows
    @DisplayName("GET search page with editor buttons")
    void getSearchPageWithEditorButtons() {
        //given
        givenSearchResults();
        givenFreemarkerConfiguration();
        givenUserCanCreate();

        //when
        mvc.perform(
            get("/{catalogue}/documents", catalogueKey)
                .queryParam("term", "carbon")
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_HTML))
            .andExpect(content().string(containsString(editorDropdownOpeningDiv)));
    }

    @Test
    @DisplayName("GET search page as html")
    @SneakyThrows
    void getSearchPageHtmlAsNonEditor() {
        //given
        givenSearchResults();
        givenFreemarkerConfiguration();

        //when
        mvc.perform(
            get("/{catalogue}/documents", catalogueKey)
                .queryParam("term", "carbon")
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_HTML))
            .andExpect(content().string(not(containsString(editorDropdownOpeningDiv))));
    }

    @Test
    @DisplayName("GET search results as JSON")
    @SneakyThrows
    void getSearchResultsJson() {
        //given
        givenCatalogue();
        givenSearchResults();

        //when
        mvc.perform(
            get("/{catalogue}/documents", catalogueKey)
                .header("remote-user", UNPRIVILEGED_USERNAME)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET search results as JSON using query parameter")
    @SneakyThrows
    void getSearchResultsJsonFromParameter() {
        //given
        givenCatalogue();
        givenSearchResults();

        //when
        mvc.perform(
            get("/{catalogue}/documents", catalogueKey)
                .header("remote-user", UNPRIVILEGED_USERNAME)
                .param("format", "json")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET search results for query")
    @SneakyThrows
    void getSearchResultsJsonWithQuery() {
        //given
        givenCatalogue();
        givenSearchResults();

        //when
        mvc.perform(
            get("/{catalogue}/documents", catalogueKey)
                .header("remote-user", UNPRIVILEGED_USERNAME)
                .accept(MediaType.APPLICATION_JSON)
                .param("term", "herring")
                .param("bbox", "coordinates")
                .param("op", "IsWithin")
                .param("page", "3")
                .param("rows", "33")

        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /{catalogue}/documents returns 400 for unknown facet field")
    @SneakyThrows
    void invalidFacetReturns400ForCatalogueSearch() {
        //given
        givenCatalogue();
        willThrow(new InvalidFacetException("Unknown facet field(s): badField"))
            .given(searcher).search(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any(), any(), any());

        //when/then
        mvc.perform(
            get("/{catalogue}/documents", catalogueKey)
                .accept(MediaType.APPLICATION_JSON)
                .param("facet", "badField|someValue")
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /documents returns 400 for unknown facet field across all catalogues")
    @SneakyThrows
    void invalidFacetReturns400ForAllCataloguesSearch() {
        //given
        willThrow(new InvalidFacetException("Unknown facet field(s): badField"))
            .given(searcher).search(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), eq("all"), any(), any());

        //when/then
        mvc.perform(
            get("/documents")
                .accept(MediaType.APPLICATION_JSON)
                .param("facet", "badField|someValue")
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("The sortField OpenAPI allowableValues match the set the query actually accepts")
    void openApiSortFieldValuesMatchTheAllowList() {
        int checked = 0;
        for (val method : SearchController.class.getDeclaredMethods()) {
            for (val parameter : method.getParameters()) {
                val requestParam = parameter.getAnnotation(RequestParam.class);
                if (requestParam == null || !SearchController.SORT_FIELD_PARAM.equals(requestParam.value())) {
                    continue;
                }
                val documented = parameter.getAnnotation(Parameter.class);
                assertThat(
                    "sortField on " + method.getName() + " should document its allowed values",
                    documented,
                    is(notNullValue())
                );
                assertThat(
                    "OpenAPI allowableValues on " + method.getName()
                        + " have drifted from SearchQuery.SORTABLE_FIELDS",
                    Set.of(documented.schema().allowableValues()),
                    equalTo(SearchQuery.SORTABLE_FIELDS)
                );
                checked++;
            }
        }
        assertThat(
            "both search endpoints should expose a documented sortField parameter",
            checked,
            equalTo(2)
        );
    }
}
