package uk.ac.ceh.gateway.catalogue.search;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.UNPRIVILEGED_USERNAME;

@WithMockCatalogueUser
@Slf4j
@ActiveProfiles("test")
@DisplayName("SearchController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=SearchController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class SearchControllerTest {
    @MockBean private SolrClient solrClient;
    @MockBean private CatalogueService catalogueService;
    @MockBean private FacetFactory facetFactory;
    @MockBean private CodeLookupService codeLookupService;
    @MockBean(name="permission") private PermissionService permissionService;
    @MockBean private ProfileService profileService;
    @MockBean private Searcher searcher;

    @Autowired private MockMvc mvc;
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

    private void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue())
            .willReturn(
                Catalogue.builder()
                    .id("default")
                    .title("test")
                    .url("https://example.com")
                    .contactUrl("")
                    .logo("eidc.png")
                    .build()
            );
    }

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
            relatedSearches
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
    @DisplayName("redirect to default catalogue")
    @SneakyThrows
    void redirectToDefaultCatalogue() {
        //given
        givenDefaultCatalogue();

        //when
        mvc.perform(get("/documents"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("location", "http://localhost/default/documents"));

        //then
        verifyNoInteractions(solrClient, facetFactory);
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

}
