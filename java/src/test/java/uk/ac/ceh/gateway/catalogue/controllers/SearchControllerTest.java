
package uk.ac.ceh.gateway.catalogue.controllers;

import java.util.Arrays;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import static org.junit.Assert.assertEquals;
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
        List<GeminiDocumentSolrIndex> mockResults = Arrays.asList(
                mock(GeminiDocumentSolrIndex.class),
                mock(GeminiDocumentSolrIndex.class)
        );
        when(solrServer.query(any(SolrQuery.class), eq(SolrRequest.METHOD.POST)).getBeans(GeminiDocumentSolrIndex.class)).thenReturn(mockResults);
        
        //When
        String searchTerm = "testterm";
        int start = 3;
        int rows = 30;
        SearchResults searchResults = searchController.searchDocuments(searchTerm, start, rows);
        
        //Then
        assertEquals("Start is wrong in results header.", start, searchResults.getHeader().getStart());
        assertEquals("Rows is wrong in results header.", rows, searchResults.getHeader().getRows());
        assertEquals("Number of search results is wrong in results header.", mockResults.size(), searchResults.getHeader().getNumFound());
    }
    
    @Test
    public void isSolrQueryBuiltCorrectly() throws SolrServerException {
        //Given
        GeminiDocumentSolrIndex doc1 = mock(GeminiDocumentSolrIndex.class);
        GeminiDocumentSolrIndex doc2 = mock(GeminiDocumentSolrIndex.class);
        List<GeminiDocumentSolrIndex> results = Arrays.asList(
                doc1,
                doc2
        );
        when(solrServer.query(any(SolrQuery.class), eq(SolrRequest.METHOD.POST)).getBeans(GeminiDocumentSolrIndex.class)).thenReturn(results);
        
        //When
        String searchTerm = "testterm";
        Integer start = 3;
        Integer rows = 30;
        searchController.searchDocuments(searchTerm, start, rows);
        
        //Then
        ArgumentCaptor<SolrQuery> solrQuery = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrServer).query(solrQuery.capture(), eq(SolrRequest.METHOD.POST));
        
        assertEquals("Incorrect search term passed to solrQuery", searchTerm, solrQuery.getValue().getQuery());
        assertEquals("Incorrect start value passed to solrQuery", start, solrQuery.getValue().getStart());
        assertEquals("Incorrect number of rows passed to solrQuery", rows, solrQuery.getValue().getRows());
    }
}
