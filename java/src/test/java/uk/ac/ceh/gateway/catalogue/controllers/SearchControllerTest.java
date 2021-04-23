package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
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

    @Autowired
    private MockMvc mockMvc;

    private final String catalogueKey = "eidc";

    @Test
    @DisplayName("redirect to default catalogue")
    @SneakyThrows
    void redirectToDefaultCatalogue() {
        //given
        given(catalogueService.defaultCatalogue())
            .willReturn(
                Catalogue.builder()
                    .id("default")
                    .title("test")
                    .url("http://example.com")
                    .build()
            );
        
        //when
        mockMvc.perform(get("/documents"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("location", "http://localhost/default/documents"));

        //then
        verifyNoInteractions(solrClient, facetFactory);
    }

    @Test
    @DisplayName("get search page as html")
    @SneakyThrows
    void getSearchPage() {
        //given
        given(catalogueService.retrieve(catalogueKey))
            .willReturn(
                Catalogue.builder()
                    .id(catalogueKey)
                    .title("Env Data Centre")
                    .url("https://example.com")
                    .build()
            );
        val response = mock(QueryResponse.class, Answers.RETURNS_DEEP_STUBS);
        given(solrClient.query(any(SolrParams.class), eq(SolrRequest.METHOD.POST)))
            .willReturn(response);

        //when
        mockMvc.perform(
            get("/{catalogue}/documents", catalogueKey).accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(view().name("/html/search.ftl"));
    }
    
}
