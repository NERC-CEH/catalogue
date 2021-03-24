package uk.ac.ceh.gateway.catalogue.controllers;

import org.apache.solr.client.solrj.SolrClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.search.FacetFactory;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SearchControllerTest {
    @Mock private SolrClient solrClient;
    @Mock private GroupStore<CatalogueUser> groupStore;
    @Mock private CatalogueService catalogueService;
    @Mock private FacetFactory facetFactory;
    private SearchController controller;
    
    @BeforeEach
    public void setup() {
        controller = new SearchController(solrClient, groupStore, catalogueService, facetFactory);
    }

    @Test
    public void redirectToDefaultCatalogue() {
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
