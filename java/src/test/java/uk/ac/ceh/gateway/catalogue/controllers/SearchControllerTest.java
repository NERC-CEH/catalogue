package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.search.FacetFactory;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@ActiveProfiles({"development"})
@ContextConfiguration(classes = {
    SearchController.class,
    DevelopmentUserStoreConfig.class,
    SecurityConfigCrowd.class
})
@DisplayName("SearchController")
@WithMockUser
@WebMvcTest(SearchController.class)
class SearchControllerTest {
    @MockBean private SolrClient solrClient;
    @MockBean private CatalogueService catalogueService;
    @MockBean private FacetFactory facetFactory;

    @Autowired private MockMvc mockMvc;

    private final String catalogueKey = "eidc";

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
    private void givenSearchResults() {
        given(solrClient.query(any(SolrParams.class), eq(SolrRequest.METHOD.POST)))
            .willReturn(mock(QueryResponse.class, Answers.RETURNS_DEEP_STUBS));
    }

    @Test
    @DisplayName("redirect to default catalogue")
    @SneakyThrows
    void redirectToDefaultCatalogue() {
        //given
        givenDefaultCatalogue();

        //when
        mockMvc.perform(get("/documents"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("location", "http://localhost/default/documents"));

        //then
        verifyNoInteractions(solrClient, facetFactory);
    }

    @Test
    @DisplayName("GET search page as html")
    @SneakyThrows
    void getSearchPageHtml() {
        //given
        givenCatalogue();
        givenSearchResults();

        //when
        mockMvc.perform(
            get("/{catalogue}/documents", catalogueKey)
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(view().name("html/search"))
            .andExpect(model().attributeExists("catalogue"))
            .andExpect(model().attributeExists("results"));
    }

    @Test
    @DisplayName("GET search results as JSON")
    @SneakyThrows
    void getSearchResultsJson() {
        //given
        givenCatalogue();
        givenSearchResults();

        //when
        mockMvc.perform(
            get("/{catalogue}/documents", catalogueKey)
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
        mockMvc.perform(
            get("/{catalogue}/documents", catalogueKey)
                .param("format", "json")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET search reults for query")
    @SneakyThrows
    void getSearchResultsJsonWithQuery() {
        //given
        givenCatalogue();
        givenSearchResults();

        //when
        mockMvc.perform(
            get("/{catalogue}/documents", catalogueKey)
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
