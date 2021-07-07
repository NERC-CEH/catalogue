package uk.ac.ceh.gateway.catalogue.controllers;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndex;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.search.FacetFactory;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
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

    @Autowired private MockMvc mvc;
    @Autowired Configuration configuration;

    private final String catalogueKey = "eidc";
    private final String editorDropdownOpeningDiv = "<div id=\"editorCreate\" class=\"dropdown\">";

    private void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue())
            .willReturn(
                Catalogue.builder()
                    .id("default")
                    .title("test")
                    .url("http://example.com")
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
                    .build()
            );
    }

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("codes", codeLookupService);
        configuration.setSharedVariable("permission", permissionService);
    }

    private void givenUserCanCreate() {
        given(permissionService.userCanCreate(catalogueKey))
            .willReturn(true);
    }

    private void givenUserCanNotCreate() {
        given(permissionService.userCanCreate(catalogueKey))
            .willReturn(false);
    }

    @SneakyThrows
    private void givenSearchResults() {
        given(solrClient.query(eq("documents"), any(SolrParams.class), eq(SolrRequest.METHOD.POST)))
            .willReturn(mock(QueryResponse.class, Answers.RETURNS_DEEP_STUBS));
    }

    @SneakyThrows
    private void givenSearchResultsWithResults() {
        val results = Arrays.asList(
            create("0"),
            create("1")
        );
        val queryResponse = mock(QueryResponse.class, Answers.RETURNS_DEEP_STUBS);
        given(queryResponse.getBeans(SolrIndex.class)).willReturn(results);
        given(solrClient.query(eq("documents"), any(SolrParams.class), eq(SolrRequest.METHOD.POST)))
            .willReturn(queryResponse);
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
        givenCatalogue();
        givenSearchResultsWithResults();
        givenFreemarkerConfiguration();
        givenUserCanCreate();

        //when
        mvc.perform(
            get("/{catalogue}/documents", catalogueKey)
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_HTML))
            .andExpect(content().string(containsString(editorDropdownOpeningDiv)));

        //then
    }

    @Test
    @DisplayName("GET search page as html")
    @SneakyThrows
    void getSearchPageHtml() {
        //given
        givenCatalogue();
        givenSearchResultsWithResults();
        givenFreemarkerConfiguration();
        givenUserCanNotCreate();

        //when
        mvc.perform(
            get("/{catalogue}/documents", catalogueKey)
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
