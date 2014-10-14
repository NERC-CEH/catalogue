package uk.ac.ceh.gateway.catalogue.search;

import java.util.Collections;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

public class SearchResultsTest {

    @Test
    public void simpleSearchResults() {
        //Given
        SearchQuery query = new SearchQuery(
            SearchQueryTest.ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            SearchQueryTest.DEFAULT_BBOX,
            SearchQueryTest.DEFAULT_PAGE,
            SearchQueryTest.DEFAULT_ROWS,
            SearchQueryTest.DEFAULT_FILTERS);
        
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
            2,
            SearchQueryTest.DEFAULT_ROWS,
            SearchQueryTest.DEFAULT_FILTERS);
        
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
            1,
            SearchQueryTest.DEFAULT_ROWS,
            SearchQueryTest.DEFAULT_FILTERS);
        
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
            2,
            20,
            SearchQueryTest.DEFAULT_FILTERS);
        
        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = spy(new SearchResults(response, query));
        doReturn(30L).when(results).getNumFound();
        
        //When
        String pageUrl = results.getNextPage();
        
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
            2,
            20,
            SearchQueryTest.DEFAULT_FILTERS);
        
        QueryResponse response = mock(QueryResponse.class);
        SearchResults results = spy(new SearchResults(response, query));
        doReturn(50L).when(results).getNumFound();
        
        //When
        String pageUrl = results.getNextPage();
        
        //Then
        assertNotNull("Expected to not get a page url", pageUrl);
        assertThat("Expected page=3 in url", pageUrl, containsString("page=3"));
    }
}
