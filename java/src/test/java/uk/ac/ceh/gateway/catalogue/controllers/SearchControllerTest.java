package uk.ac.ceh.gateway.catalogue.controllers;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.solr.client.solrj.SolrServer;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.Before;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.search.FacetFactory;
import uk.ac.ceh.gateway.catalogue.search.FacetFilter;
import uk.ac.ceh.gateway.catalogue.search.SearchQuery;
import uk.ac.ceh.gateway.catalogue.search.SearchResults;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;

public class SearchControllerTest {
    @Mock private SolrServer solrServer;
    @Mock private GroupStore groupStore;
    @Mock private CatalogueService catalogueService;
    @Mock private FacetFactory facetFactory;
    private SearchController controller;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        controller = new SearchController(solrServer, groupStore, catalogueService, facetFactory);
    }

    @Test
    public void redriectToDefaultCatalogue() {
        //given
        HttpServletRequest request = new MockHttpServletRequest(
            "GET",
            "/documents"
        );
        given(catalogueService.defaultCatalogue())
            .willReturn(
                Catalogue.builder()
                    .id("default")
                    .title("test")
                    .url("http://example.com")
                    .build()
            );
        
        //when
        RedirectView actual = controller.redirectToDefaultCatalogue(request);
        
        //then
        verify(catalogueService).defaultCatalogue();
        assertThat(
            "Redirect url should be: http://localhost/default/documents",
            actual.getUrl(),
            equalTo("http://localhost/default/documents")
        );
    }
    
}
