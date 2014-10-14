package uk.ac.ceh.gateway.catalogue.search;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

public class SearchQueryTest {
    public static final String ENDPOINT = "http://catalogue.com/documents";
    public static final String DEFAULT_BBOX = null;
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_ROWS = 20;
    public static final List<FacetFilter> DEFAULT_FITLERS = Collections.EMPTY_LIST;
    
    @Test
    public void buildQueryWithNoExtraParameters() {
        //Given
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            DEFAULT_FITLERS);
        //When
        SolrQuery solrQuery = query.build();
        
        //Then
        assertThat("Solr query should be the 'default text'", solrQuery.getQuery(), equalTo(SearchQuery.DEFAULT_SEARCH_TERM));
        assertThat("Solr query state filter query should be 'public'", solrQuery.getFilterQueries(), hasItemInArray("{!term f=state}public"));
        assertThat("Solr query isOgl facet fields should be present", solrQuery.getFacetFields(), hasItemInArray("isOgl"));
        assertThat("Solr query resourceType facet fields should be present", solrQuery.getFacetFields(), hasItemInArray("resourceType"));
        assertThat("Solr query start should be 0 for first page", solrQuery.getStart(), equalTo(0));
        assertThat("Solr query rows should be default", solrQuery.getRows(), equalTo(DEFAULT_ROWS));
        assertThat("Solr query facet min count should be set", solrQuery.getFacetMinCount(), equalTo(1));
        assertThat("Solr query sort order should be 'random'", solrQuery.getSorts().get(0).getItem().substring(0, 6), equalTo("random"));
    }
    
    @Test
    public void buildQueryOnSecondPage() {
        //Given
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,
            2,
            40,
            DEFAULT_FITLERS);
        
        //When
        SolrQuery solrQuery = query.build();
        
        //Then
        assertThat("Expected to be in the search results by the row count", solrQuery.getStart(), equalTo(40));
        assertThat("Solr query rows should be 40", solrQuery.getRows(), equalTo(40));
    }
    
    @Test
    public void buildQueryWithSimpleTerm() {
        //Given
        String term = "land cover";
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            term,
            DEFAULT_BBOX,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            DEFAULT_FITLERS);
        //When
        SolrQuery solrQuery = query.build();
        
        //Then
        assertThat("Solr query should be the default text", solrQuery.getQuery(), equalTo(term));
        assertThat("Solr query sort order should not be 'random'", solrQuery.getSorts().isEmpty(), equalTo(true));
    }
    
    @Test
    public void buildQueryWithDefaultTermAndFilter() {
        //Given
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            Arrays.asList(
                new FacetFilter("resourceType","dataset"),
                new FacetFilter("sci0","Green & yellow")));
        //When
        SolrQuery solrQuery = query.build();
        
        //Then
        assertThat("Solr query should be the default text", solrQuery.getQuery(), equalTo(SearchQuery.DEFAULT_SEARCH_TERM));
        assertThat("Solr query should have resourceType filter", solrQuery.getFilterQueries(), hasItemInArray("{!term f=resourceType}dataset"));
        assertThat("Solr query should have sci0 filter", solrQuery.getFilterQueries(), hasItemInArray("{!term f=sci0}Green & yellow"));
    }
    
    @Test
    public void loggedInUserCanOnlySearchForPublicRecords() {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("testloggedin");
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            user,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,                
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            DEFAULT_FITLERS);

        //When
        SolrQuery solrQuery = query.build();

        //Then
        assertThat("FilterQuery should be 'state:public' for logged in user", solrQuery.getFilterQueries(), hasItemInArray("{!term f=state}public"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void exceptionThrownWhenBBOXIsContainsText() {
        //Given
        String bbox = "my,invalid,bbox,attempt";
        
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            bbox,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            DEFAULT_FITLERS);
        
        //When
        SolrQuery solrQuery = query.build();
        
        //Then
        fail("Expected to get an illegal argument exception");
    }
    
    @Test
    public void noExceptionThrownWhenBBoxIsValid() {
        //Given
        String bbox = "1.11,2.22,3.33,4.44";
        
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            bbox,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            DEFAULT_FITLERS);
        
        //When
        SolrQuery solrQuery = query.build();
        
        //Then
        assertThat("Expected to fild a solr bbox filter", solrQuery.getFilterQueries(), hasItemInArray("locations:\"isWithin(1.11 2.22 3.33 4.44)\""));
    }
    
}
