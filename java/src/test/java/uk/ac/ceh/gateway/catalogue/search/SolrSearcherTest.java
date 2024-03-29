package uk.ac.ceh.gateway.catalogue.search;

import lombok.SneakyThrows;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SolrSearcherTest {
    @Mock private SolrClient solrClient;
    @Mock private GroupStore<CatalogueUser> groupStore;
    @Mock private CatalogueService catalogueService;
    @Mock private FacetFactory facetFactory;

    private Searcher searcher;

    private final String endpoint = "https://example.com";
    private final CatalogueUser user = CatalogueUser.PUBLIC_USER;
    private final String term = "carbon";
    private final String bbox = "()";
    private final SpatialOperation spatialOperation = SpatialOperation.ISWITHIN;
    private final int page = 1;
    private final int rows = 20;
    private final List<FacetFilter> facetFilters = List.of(new FacetFilter("filter|test"));
    private final String catalogueKey = "green";

    @BeforeEach
    void setup() {
        searcher = new SolrSearcher(
            solrClient,
            groupStore,
            catalogueService,
            facetFactory
        );
    }

    @Test
    void search() {
        //given
        givenCatalogue();
        givenFacets();
        givenSolrQuery();

        //when
        searcher.search(
            endpoint,
            user,
            term,
            bbox,
            spatialOperation,
            page,
            rows,
            facetFilters,
            catalogueKey
        );

        //then
    }

    @SneakyThrows
    private void givenSolrQuery() {
        given(solrClient.query(
            eq("documents"),
            any(SolrParams.class),
            eq(SolrRequest.METHOD.POST)
        ))
            .willReturn(mock(QueryResponse.class));
    }

    private void givenFacets() {
        given(facetFactory.newInstances(List.of("facet1", "facet2")))
            .willReturn(List.of(
                Facet.builder().fieldName("facet1").build(),
                Facet.builder().fieldName("facet2").build()
            ));
    }

    private void givenCatalogue() {
        given(catalogueService.retrieve(catalogueKey))
            .willReturn(Catalogue.builder()
                .id(catalogueKey)
                .title("title")
                .url("https://example.com")
                .contactUrl("https://example.com/aboutus")
                .logo("eidc.png")
                .facetKey("facet1")
                .facetKey("facet2")
                .build()
            );
    }

}
