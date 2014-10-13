package uk.ac.ceh.gateway.catalogue.search;

import java.util.Collections;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

public class DocumentSearchResultsTest {

    @Test
    public void simpleSearchResults() {
        //Given
        SearchQuery query = new SearchQuery(
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            SearchQueryTest.DEFAULT_BBOX,
            SearchQueryTest.DEFAULT_PAGE,
            SearchQueryTest.DEFAULT_ROWS,
            SearchQueryTest.DEFAULT_FITLERS);
        
        QueryResponse response = mock(QueryResponse.class);
        long resultFound = 34553450359345l;
        SolrDocumentList results = mock(SolrDocumentList.class);
        given(response.getResults()).willReturn(results);
        given(results.getNumFound()).willReturn(resultFound);
        NamedList pivots = mock(NamedList.class);
        given(response.getFacetPivot()).willReturn(pivots);
        given(pivots.get("sci0,sci1")).willReturn(Collections.EMPTY_LIST);
        
        //When
        DocumentSearchResults searchResults = new DocumentSearchResults(response, query, UriComponentsBuilder.fromHttpUrl("https://localhost:8080/documents"));
        
        //Then
        assertThat("Term is wrong in results", searchResults.getTerm(), equalTo(""));
        assertThat("Page is wrong in results", searchResults.getPage(), equalTo(SearchQueryTest.DEFAULT_PAGE));
        assertThat("Rows is wrong in results", searchResults.getRows(), equalTo(SearchQueryTest.DEFAULT_ROWS));
        assertThat("Number of search results is wrong in results", searchResults.getNumFound(), equalTo(resultFound));
    }
    
}
