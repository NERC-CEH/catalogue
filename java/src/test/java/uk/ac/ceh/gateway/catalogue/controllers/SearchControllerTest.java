
package uk.ac.ceh.gateway.catalogue.controllers;

import java.util.ArrayList;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocumentSolrIndexGenerator.GeminiDocumentSolrIndex;
import uk.ac.ceh.gateway.catalogue.model.SearchResults;

public class SearchControllerTest {
    
    
    private SearchController searchController;
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) SolrServer solrServer;
    
    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
        searchController = new SearchController(solrServer);
    }
    
    @Test
    public void isHeaderCorrect() throws SolrServerException{
        //Given
        SolrDocumentList results = mock(SolrDocumentList.class);
        when(results.getNumFound()).thenReturn(100L);
        when(solrServer.query(any(SolrQuery.class), eq(SolrRequest.METHOD.POST)).getResults()).thenReturn(results);
        
        //When
        String searchTerm = "testterm";
        int start = 2;
        int rows =5;
        String facet = "";
        SearchResults searchResults = searchController.searchDocuments(searchTerm, start, rows);
        
        //Then
        assertEquals("Term is wrong in results header.", searchTerm, searchResults.getHeader().getTerm());
        assertEquals("Start is wrong in results header.", start, searchResults.getHeader().getStart());
        assertEquals("Rows is wrong in results header.", rows, searchResults.getHeader().getRows());
        assertEquals("Number of search results is wrong in results header.", 100L, searchResults.getHeader().getNumFound());
    }
    
    @Test
    public void isSolrQueryBuiltCorrectly() throws SolrServerException {
        //Given
        List<GeminiDocumentSolrIndex> mockResults = new ArrayList<>();
        for(int i =0; i<9; i++){
            mockResults.add(mock(GeminiDocumentSolrIndex.class));
        }
        when(solrServer.query(any(SolrQuery.class), eq(SolrRequest.METHOD.POST)).getBeans(GeminiDocumentSolrIndex.class)).thenReturn(mockResults);
        
        //When
        String searchTerm = "testterm";
        Integer start = 3;
        Integer rows = 7;
        searchController.searchDocuments(searchTerm, start, rows);
        
        //Then
        ArgumentCaptor<SolrQuery> solrQuery = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrServer).query(solrQuery.capture(), eq(SolrRequest.METHOD.POST));
        
        assertEquals("Incorrect search term passed to solrQuery", searchTerm, solrQuery.getValue().getQuery());
        assertEquals("Incorrect start value passed to solrQuery", start, solrQuery.getValue().getStart());
        assertEquals("Incorrect number of rows passed to solrQuery", rows, solrQuery.getValue().getRows());
    }
    
    @Test
    public void facetMinCountIsSet() throws SolrServerException{
        //When
        searchController.searchDocuments("testterm", 1, 10);
        ArgumentCaptor<SolrQuery> solrQuery = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrServer).query(solrQuery.capture(), eq(SolrRequest.METHOD.POST));
        
        //Then
        int expected = 1;
        int actual = solrQuery.getValue().getFacetMinCount();
        assertEquals("Expected minimum facet size of " + expected + ", got " + actual, expected, actual);
    }
    
    @Test
    public void isRandomSortWhenTermIsDefault() throws SolrServerException{
        //When 
        searchController.searchDocuments(SearchController.DEFAULT_SEARCH_TERM, 1, 10);

        //Then
        ArgumentCaptor<SolrQuery> solrQuery = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrServer).query(solrQuery.capture(), eq(SolrRequest.METHOD.POST));
        List<SortClause> sortClauses = solrQuery.getValue().getSorts();
        
        assertNotNull("Didn't expect sortClauses to be null", sortClauses);
        assertTrue("Expected at least one sort clause", sortClauses.size() > 0);
        assertEquals("Expected sort order to use dynamic random field", "random", sortClauses.get(0).getItem().substring(0, 6));
    }
    
    @Test
    public void isNotRandomSortWhenTermIsNotDefault() throws SolrServerException{
        //When 
        searchController.searchDocuments("Not default search term", 1, 10);

        //Then
        ArgumentCaptor<SolrQuery> solrQuery = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrServer).query(solrQuery.capture(), eq(SolrRequest.METHOD.POST));
        List<SortClause> sortClauses = solrQuery.getValue().getSorts();
        
        assertNotNull("Didn't expect sortClauses to be null", sortClauses);
        assertEquals("Did not expect any sortClauses", 0, sortClauses.size());
    }
}