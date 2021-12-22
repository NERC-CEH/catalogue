package uk.ac.ceh.gateway.catalogue.search;

import lombok.val;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Link;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SearchResultsTest {
    @Mock private GroupStore<CatalogueUser> groupStore;

    @Test
    public void facetResultsArePresent() {
        //Given
        SearchQuery query = new SearchQuery(
            SearchQueryTest.ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            SearchQueryTest.DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            SearchQueryTest.DEFAULT_PAGE,
            SearchQueryTest.DEFAULT_ROWS,
            SearchQueryTest.DEFAULT_FILTERS,
            groupStore,
            Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
            SearchQueryTest.DEFAULT_FACETS
        );

        QueryResponse response = mock(QueryResponse.class);

        //When
        new SearchResults(response, query, Collections.emptyList());

        //Then
        verify(response).getFacetField("resourceType");
        verify(response).getFacetField("licence");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void simpleSearchResults() {
        //Given
        SearchQuery query = new SearchQuery(
            SearchQueryTest.ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            SearchQueryTest.DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            SearchQueryTest.DEFAULT_PAGE,
            SearchQueryTest.DEFAULT_ROWS,
            SearchQueryTest.DEFAULT_FILTERS,
            groupStore,
            Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
            SearchQueryTest.DEFAULT_FACETS
        );

        QueryResponse response = mock(QueryResponse.class);
        long resultFound = 34553450359345L;
        SolrDocumentList results = mock(SolrDocumentList.class);
        given(response.getResults()).willReturn(results);
        given(results.getNumFound()).willReturn(resultFound);
        val pivots = mock(NamedList.class);
        given(response.getFacetPivot()).willReturn(pivots);
        given(pivots.get("sci0,sci1")).willReturn(Collections.EMPTY_LIST);

        //When
        SearchResults searchResults = new SearchResults(response, query, Collections.emptyList());

        //Then
        assertThat("Term is wrong in results", searchResults.getTerm(), equalTo(""));
        assertThat("Page is wrong in results", searchResults.getPage(), equalTo(SearchQueryTest.DEFAULT_PAGE));
        assertThat("Rows is wrong in results", searchResults.getRows(), equalTo(SearchQueryTest.DEFAULT_ROWS));
        assertThat("Number of search results is wrong in results", searchResults.getNumFound(), equalTo(resultFound));
    }

    @Test
    public void checkThatPrevPageIsPresentOnSecondPage() {
        //Given
        SearchQuery query = new SearchQuery(
            SearchQueryTest.ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            SearchQueryTest.DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            2,
            SearchQueryTest.DEFAULT_ROWS,
            SearchQueryTest.DEFAULT_FILTERS,
            groupStore,
            Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
            SearchQueryTest.DEFAULT_FACETS
        );

        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = new SearchResults(response, query, Collections.emptyList());

        //When
        String pageUrl = results.getPrevPage();

        //Then
        assertThat(pageUrl, not(nullValue()));
    }

    @Test
    public void checkThatPrevPageIsNotShownOnFirstPage() {
        //Given
        SearchQuery query = new SearchQuery(
            SearchQueryTest.ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            SearchQueryTest.DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            1,
            SearchQueryTest.DEFAULT_ROWS,
            SearchQueryTest.DEFAULT_FILTERS,
            groupStore,
            Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
            SearchQueryTest.DEFAULT_FACETS
        );

        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = new SearchResults(response, query, Collections.emptyList());

        //When
        String pageUrl = results.getPrevPage();

        //Then
        assertNull(pageUrl);
    }

    @Test
    public void checkThatNoNextPageIsShownWhenOnLastPage() {
        //Given
        SearchQuery query = new SearchQuery(
            SearchQueryTest.ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            SearchQueryTest.DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            2,
            20,
            SearchQueryTest.DEFAULT_FILTERS,
            groupStore,
            Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
            SearchQueryTest.DEFAULT_FACETS
        );

        QueryResponse response = mock(QueryResponse.class);
        SolrDocumentList solrDocumentList = mock(SolrDocumentList.class);
        given(response.getResults()).willReturn(solrDocumentList);
        given(solrDocumentList.getNumFound()).willReturn(30L);

        //When
        String pageUrl = new SearchResults(response, query, Collections.emptyList()).getNextPage();

        //Then
        assertNull(pageUrl);
    }

    @Test
    public void checkNextPageIsShownWhenThereAreMoreResults() {
        //Given
        SearchQuery query = new SearchQuery(
            SearchQueryTest.ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            SearchQueryTest.DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            2,
            20,
            SearchQueryTest.DEFAULT_FILTERS,
            groupStore,
            Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
            SearchQueryTest.DEFAULT_FACETS
        );

        QueryResponse response = mock(QueryResponse.class);
        SolrDocumentList solrDocumentList = mock(SolrDocumentList.class);
        given(response.getResults()).willReturn(solrDocumentList);
        given(solrDocumentList.getNumFound()).willReturn(50L);

        //When
        String pageUrl = new SearchResults(response, query, Collections.emptyList()).getNextPage();

        //Then
        assertThat(pageUrl, not(nullValue()));
        assertThat("Expected page=3 in url", pageUrl, containsString("page=3"));
    }

    @Test
    public void checkThatNoWithoutBoundingBoxUrlIsGeneratedIfNotFilteringWithBoundingBox() {
        //Given
        SearchQuery query = new SearchQuery(
            SearchQueryTest.ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            SearchQueryTest.DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            2,
            20,
            SearchQueryTest.DEFAULT_FILTERS,
            groupStore,
            Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
            SearchQueryTest.DEFAULT_FACETS
        );

        //When
        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = new SearchResults(response, query, Collections.emptyList());
        String url = results.getWithoutBbox();

        //Then
        assertNull(url);
    }

    @Test
    public void checkThatWithoutBBoxUrlIsGeneratedWhenBBoxIsApplied() {
        //Given
        SearchQuery query = new SearchQuery(
            SearchQueryTest.ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            "10,23,23,40",
            SpatialOperation.ISWITHIN,
            2,
            20,
            SearchQueryTest.DEFAULT_FILTERS,
            groupStore,
            Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
            SearchQueryTest.DEFAULT_FACETS
        );

        //When
        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = new SearchResults(response, query, Collections.emptyList());
        String url = results.getWithoutBbox();

        //Then
        assertThat(url, not(nullValue()));
        assertThat("Didn't expect bbox to be applied", url, not(containsString("bbox")));
    }

    @Test
    public void checkThatIsWithinUrlIsPresentWhenUsingIntersectsOperation() {
        //Given
        SearchQuery query = new SearchQuery(
            SearchQueryTest.ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            "10,23,23,40",
            SpatialOperation.INTERSECTS,
            2,
            20,
            SearchQueryTest.DEFAULT_FILTERS,
            groupStore,
            Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
            SearchQueryTest.DEFAULT_FACETS
        );


        //When
        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = new SearchResults(response, query, Collections.emptyList());
        String url = results.getWithinBbox();

        //Then
        assertThat(url, not(nullValue()));
        assertThat("Expected url to contain other filter", url, containsString(SpatialOperation.ISWITHIN.getOperation()));
    }

    @Test
    public void checkThatIsWithinUrlIsntPresentWhenUsingIsWithinOperation() {
        //Given
        SearchQuery query = new SearchQuery(
            SearchQueryTest.ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            "10,23,23,40",
            SpatialOperation.ISWITHIN,
            2,
            20,
            SearchQueryTest.DEFAULT_FILTERS,
            groupStore,
            Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
            SearchQueryTest.DEFAULT_FACETS
        );


        //When
        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = new SearchResults(response, query, Collections.emptyList());
        String url = results.getWithinBbox();

        //Then
        assertNull(url);
    }

    @Test
    public void checkThatIntersectingUrlIsPresentWhenUsingIsWithinOperation() {
        //Given
        SearchQuery query = new SearchQuery(
            SearchQueryTest.ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            "10,23,23,40",
            SpatialOperation.ISWITHIN,
            2,
            20,
            SearchQueryTest.DEFAULT_FILTERS,
            groupStore,
            Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
            SearchQueryTest.DEFAULT_FACETS
        );


        //When
        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = new SearchResults(response, query, Collections.emptyList());
        String url = results.getIntersectingBbox();

        //Then
        assertThat(url, not(nullValue()));
        assertThat("Expected url to contain other filter", url, containsString(SpatialOperation.INTERSECTS.getOperation()));
    }

    @Test
    public void checkThatIntersectingUrlIsNotPresentWhenUsingIntersectingOperation() {
        //Given
        SearchQuery query = new SearchQuery(
            SearchQueryTest.ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            "10,23,23,40",
            SpatialOperation.INTERSECTS,
            2,
            20,
            SearchQueryTest.DEFAULT_FILTERS,
            groupStore,
            Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
            SearchQueryTest.DEFAULT_FACETS
        );


        //When
        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = new SearchResults(response, query, Collections.emptyList());
        String url = results.getIntersectingBbox();

        //Then
        assertNull(url);
    }

    @Test
    public void checkThatSwitchingSpatialOperationJumpsToPageOne() {
        //Given
        int page = 400;
        SearchQuery query = new SearchQuery(
            SearchQueryTest.ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            "10,23,23,40",
            SpatialOperation.INTERSECTS,
            page,
            20,
            SearchQueryTest.DEFAULT_FILTERS,
            groupStore,
            Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
            SearchQueryTest.DEFAULT_FACETS
        );

        //When
        SearchQuery newQuery = query.withSpatialOperation(SpatialOperation.ISWITHIN);

        //Then
        assertThat("Isn't on page 400", newQuery.getPage(), not(equalTo(page)));
    }

    @Test
    public void checkThatRelatedSearchesArePresent() {
        //Given
        SearchQuery query = new SearchQuery(
            SearchQueryTest.ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            SearchQueryTest.DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            2,
            SearchQueryTest.DEFAULT_ROWS,
            SearchQueryTest.DEFAULT_FILTERS,
            groupStore,
            Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
            SearchQueryTest.DEFAULT_FACETS
        );

        QueryResponse response = mock(QueryResponse.class);
        val relatedLinks = List.of(
            Link.builder().href("https://example.com/1").build(),
            Link.builder().href("https://example.com/2").build()
        );
        SearchResults results = new SearchResults(response, query, relatedLinks);

        //When
        val relatedSearches = results.getRelatedSearches();

        //Then
        assertThat(relatedSearches, not(nullValue()));
    }
}
