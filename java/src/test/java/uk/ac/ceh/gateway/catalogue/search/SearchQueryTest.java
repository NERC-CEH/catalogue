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
    public static final int DEFAULT_START = 0;
    public static final int DEFAULT_ROWS = 20;
    public static final List<String> DEFAULT_FITLERS = Collections.EMPTY_LIST;
    
    @Test
    public void buildQueryWithNoExtraParameters() {
        //Given
        SearchQuery query = new SearchQuery(
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_START,
            DEFAULT_ROWS,
            DEFAULT_FITLERS);
        //When
        SolrQuery solrQuery = query.build();
        
        //Then
        assertThat("Solr query should be the 'default text'", solrQuery.getQuery(), equalTo(SearchQuery.DEFAULT_SEARCH_TERM));
        assertThat("Solr query state filter query should be 'public'", solrQuery.getFilterQueries(), hasItemInArray("{!term f=state}public"));
        assertThat("Solr query isOgl facet fields should be present", solrQuery.getFacetFields(), hasItemInArray("isOgl"));
        assertThat("Solr query resourceType facet fields should be present", solrQuery.getFacetFields(), hasItemInArray("resourceType"));
        assertThat("Solr query start should be default", solrQuery.getStart(), equalTo(DEFAULT_START));
        assertThat("Solr query rows should be default", solrQuery.getRows(), equalTo(DEFAULT_ROWS));
        assertThat("Solr query facet min count should be set", solrQuery.getFacetMinCount(), equalTo(1));
        assertThat("Solr query sort order should be 'random'", solrQuery.getSorts().get(0).getItem().substring(0, 6), equalTo("random"));
    }
    
    @Test
    public void buildQueryWithSimpleTerm() {
        //Given
        String term = "land cover";
        SearchQuery query = new SearchQuery(
            CatalogueUser.PUBLIC_USER,
            term,
            DEFAULT_START,
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
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_START,
            DEFAULT_ROWS,
            Arrays.asList("resourceType|dataset", "sci0|Green & yellow"));
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
            user,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_START,
            DEFAULT_ROWS,
            DEFAULT_FITLERS);

        //When
        SolrQuery solrQuery = query.build();

        //Then
        assertThat("FilterQuery should be 'state:public' for logged in user", solrQuery.getFilterQueries(), hasItemInArray("{!term f=state}public"));
    }
    
}
