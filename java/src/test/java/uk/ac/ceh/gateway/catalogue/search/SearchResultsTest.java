package uk.ac.ceh.gateway.catalogue.search;

import java.util.Collections;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;
import uk.ac.ceh.gateway.catalogue.services.HardcodedCatalogueService;

public class SearchResultsTest {
    @Mock private GroupStore<CatalogueUser> groupStore;
    private final CatalogueService catalogueService = new HardcodedCatalogueService();
    
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
            catalogueService.retrieve("catalogue.ceh.ac.uk"),
            SearchQueryTest.DEFAULT_FACETS
        );
        
        QueryResponse response = mock(QueryResponse.class);
                
        //When
        new SearchResults(response, query).getFacets();
        
        //Then
        verify(response).getFacetField("resourceType");
        verify(response).getFacetField("licence");
    }

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
            catalogueService.retrieve("catalogue.ceh.ac.uk"),
            SearchQueryTest.DEFAULT_FACETS
        );
        
        QueryResponse response = mock(QueryResponse.class);
        long resultFound = 34553450359345l;
        SolrDocumentList results = mock(SolrDocumentList.class);
        given(response.getResults()).willReturn(results);
        given(results.getNumFound()).willReturn(resultFound);
        NamedList pivots = mock(NamedList.class);
        given(response.getFacetPivot()).willReturn(pivots);
        given(pivots.get("sci0,sci1")).willReturn(Collections.EMPTY_LIST);
        
        //When
        SearchResults searchResults = new SearchResults(response, query);
        
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
            catalogueService.retrieve("catalogue.ceh.ac.uk"),
            SearchQueryTest.DEFAULT_FACETS
        );
        
        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = new SearchResults(response, query);
        
        //When
        String pageUrl = results.getPrevPage();
        
        //Then
        assertNotNull("Expected a url which is not null", pageUrl);
    }
    
    @Test
    public void checkThatPrevPageIsntShownOnFirstPage() {
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
            catalogueService.retrieve("catalogue.ceh.ac.uk"),
            SearchQueryTest.DEFAULT_FACETS
        );
        
        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = new SearchResults(response, query);
        
        //When
        String pageUrl = results.getPrevPage();
        
        //Then
        assertNull("Expected to not get a page url", pageUrl);
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
            catalogueService.retrieve("catalogue.ceh.ac.uk"),
            SearchQueryTest.DEFAULT_FACETS
        );
        
        QueryResponse response = mock(QueryResponse.class);
        SolrDocumentList solrDocumentList = mock(SolrDocumentList.class);
        given(response.getResults()).willReturn(solrDocumentList);
        given(solrDocumentList.getNumFound()).willReturn(30L);
        
        //When
        String pageUrl = new SearchResults(response, query).getNextPage();
        
        //Then
        assertNull("Expected to not get a page url", pageUrl);
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
            catalogueService.retrieve("catalogue.ceh.ac.uk"),
            SearchQueryTest.DEFAULT_FACETS
        );
        
        QueryResponse response = mock(QueryResponse.class);
        SolrDocumentList solrDocumentList = mock(SolrDocumentList.class);
        given(response.getResults()).willReturn(solrDocumentList);
        given(solrDocumentList.getNumFound()).willReturn(50L);
        
        //When
        String pageUrl = new SearchResults(response, query).getNextPage();
        
        //Then
        assertNotNull("Expected to not get a page url", pageUrl);
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
            catalogueService.retrieve("catalogue.ceh.ac.uk"),
            SearchQueryTest.DEFAULT_FACETS
        );
        
        //When
        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = new SearchResults(response, query);
        String url = results.getWithoutBbox();
        
        //Then
        assertNull("Expected to not get a url for without bbox", url);
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
            catalogueService.retrieve("catalogue.ceh.ac.uk"),
            SearchQueryTest.DEFAULT_FACETS
        );
        
        //When
        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = new SearchResults(response, query);
        String url = results.getWithoutBbox();
        
        //Then
        assertNotNull("Expected to not get a page url", url);
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
            catalogueService.retrieve("catalogue.ceh.ac.uk"),
            SearchQueryTest.DEFAULT_FACETS
        );
        
        
        //When
        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = new SearchResults(response, query);
        String url = results.getWithinBbox();
        
        //Then
        assertNotNull("Expected to a url", url);
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
            catalogueService.retrieve("catalogue.ceh.ac.uk"),
            SearchQueryTest.DEFAULT_FACETS
        );
        
        
        //When
        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = new SearchResults(response, query);
        String url = results.getWithinBbox();
        
        //Then
        assertNull("Expected not to get a url", url);
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
            catalogueService.retrieve("catalogue.ceh.ac.uk"),
            SearchQueryTest.DEFAULT_FACETS
        );
        
        
        //When
        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = new SearchResults(response, query);
        String url = results.getIntersectingBbox();
        
        //Then
        assertNotNull("Expected to a url", url);
        assertThat("Expected url to contain other filter", url, containsString(SpatialOperation.INTERSECTS.getOperation()));
    }
    
    @Test
    public void checkThatIntersectingUrlIsntPresentWhenUsingIntersectingOperation() {
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
            catalogueService.retrieve("catalogue.ceh.ac.uk"),
            SearchQueryTest.DEFAULT_FACETS
        );
        
        
        //When
        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = new SearchResults(response, query);
        String url = results.getIntersectingBbox();
        
        //Then
        assertNull("Expected not to get a url", url);
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
            catalogueService.retrieve("catalogue.ceh.ac.uk"),
            SearchQueryTest.DEFAULT_FACETS
        );
        
        //When
        SearchQuery newQuery = query.withSpatialOperation(SpatialOperation.ISWITHIN);
        
        //Then
        assertThat("Isn't on page 400", newQuery.getPage(), not(equalTo(page)));
    }
}
