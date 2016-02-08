package uk.ac.ceh.gateway.catalogue.search;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

public class SearchQueryTest {
    public static final String ENDPOINT = "http://catalogue.com/documents";
    public static final String DEFAULT_BBOX = null;
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_ROWS = 20;
    public static final List<FacetFilter> DEFAULT_FILTERS = Collections.EMPTY_LIST;
    @Mock private GroupStore<CatalogueUser> groupStore; 
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void loggedInUserHasUsernameAsViewFilter() {
        //Given
        CatalogueUser user = new CatalogueUser().setUsername("helen");
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            user,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            DEFAULT_FILTERS,
            groupStore
        );
        
        //When
        SolrQuery solrQuery = query.build();
        
        //Then
        assertThat("Solr query should have view filter", solrQuery.getFilterQueries(), hasItemInArray("view:(public OR helen)")); 
    }
    
    @Test
    public void loggedInUserWithGroupsHasUsernameAndGroupsAsViewFilter() {
        //Given
        CatalogueUser user = new CatalogueUser().setUsername("helen");
        given(groupStore.getGroups(user)).willReturn(Arrays.asList(createGroup("CEH"), createGroup("EIDC")));
        
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            user,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            DEFAULT_FILTERS,
            groupStore
        );
        
        //When
        SolrQuery solrQuery = query.build();
        
        //Then
        assertThat("Solr query should have view filter", solrQuery.getFilterQueries(), hasItemInArray("view:(public OR helen OR ceh OR eidc)")); 
    }
    
    @Test
    public void publisherDoesNotHaveViewFilter() {
        //Given
        CatalogueUser user = new CatalogueUser().setUsername("publisher");
        given(groupStore.getGroups(user)).willReturn(Arrays.asList(createGroup("ROLE_CIG_PUBLISHER")));
        
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            user,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            DEFAULT_FILTERS,
            groupStore
        );
        
        //When
        SolrQuery solrQuery = query.build();
        
        //Then
        assertThat("Solr query should have view filter", solrQuery.getFilterQueries(), not(hasItemInArray("view:(public OR publisher OR role_cig_publisher)")));
    }
    
    private Group createGroup(String name) {
        return new Group() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDescription() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
    
    @Test
    public void buildQueryWithNoExtraParameters() {
        //Given
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            DEFAULT_FILTERS,
            groupStore
        );
        //When
        SolrQuery solrQuery = query.build();
        
        //Then
        assertThat("Solr query should be the 'default text'", solrQuery.getQuery(), equalTo(SearchQuery.DEFAULT_SEARCH_TERM));
        assertThat("Solr query state filter query should be 'public'", solrQuery.getFilterQueries(), hasItemInArray("{!term f=view}public"));
        assertThat("Solr query state filter query should be 'published'", solrQuery.getFilterQueries(), hasItemInArray("{!term f=state}published"));
        assertThat("Solr query licence facet fields should be present", solrQuery.getFacetFields(), hasItemInArray("licence"));
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
            SpatialOperation.ISWITHIN,
            2,
            40,
            DEFAULT_FILTERS,
            groupStore
        );
        
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
            SpatialOperation.ISWITHIN,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            DEFAULT_FILTERS,
            groupStore
        );
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
            SpatialOperation.ISWITHIN,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            Arrays.asList(
                new FacetFilter("resourceType","dataset"),
                new FacetFilter("topic","0/Climate/")),
            groupStore
        );
        //When
        SolrQuery solrQuery = query.build();
        
        //Then
        assertThat("Solr query should be the default text", solrQuery.getQuery(), equalTo(SearchQuery.DEFAULT_SEARCH_TERM));
        assertThat("Solr query should have resourceType filter", solrQuery.getFilterQueries(), hasItemInArray("{!term f=resourceType}dataset"));
        assertThat("Solr query should have topic filter", solrQuery.getFilterQueries(), hasItemInArray("{!term f=topic}0/Climate/"));
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
            SpatialOperation.ISWITHIN,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            DEFAULT_FILTERS,
            groupStore
        );
        
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
            SpatialOperation.ISWITHIN,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            DEFAULT_FILTERS,
            groupStore
        );
        
        //When
        SolrQuery solrQuery = query.build();
        
        //Then
        assertThat("Expected to fild a solr bbox filter", solrQuery.getFilterQueries(), hasItemInArray("locations:\"IsWithin(1.11 2.22 3.33 4.44)\""));
    }
    
     @Test
    public void canSetIntersectBBox() {
        //Given
        String bbox = "1.11,2.22,3.33,4.44";
        
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            bbox,
            SpatialOperation.INTERSECTS,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            DEFAULT_FILTERS,
            groupStore
        );
        
        //When
        SolrQuery solrQuery = query.build();
        
        //Then
        assertThat("Expected to fild a solr bbox filter", solrQuery.getFilterQueries(), hasItemInArray("locations:\"Intersects(1.11 2.22 3.33 4.44)\""));
    }
    
    @Test
    public void checkThatWithFacetReturnsToFirstPage() {
        //Given
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            18,
            DEFAULT_ROWS,
            DEFAULT_FILTERS,
            groupStore
        );
        
        //When
        SearchQuery queryWithFacet = query.withFacetFilter(new FacetFilter("what", "ever"));
        
        //Then
        assertThat("Expected to be back on first page", queryWithFacet.getPage(), equalTo(1));
    }
    
        
    @Test
    public void checkThatWithoutFacetReturnsToFirstPage() {
        //Given
        FacetFilter filter = new FacetFilter("what", "ever");
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            18,
            DEFAULT_ROWS,
            Arrays.asList(filter),
            groupStore
        );
        
        //When
        SearchQuery queryWithFacet = query.withoutFacetFilter(filter);
        
        //Then
        assertThat("Expected to be back on first page", queryWithFacet.getPage(), equalTo(1));
    }
    
    @Test
    public void checkThatWithFacetFilterAddsNewFilter() {
        //Given
        FacetFilter filter = new FacetFilter("what", "ever");
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            18,
            DEFAULT_ROWS,
            DEFAULT_FILTERS,
            groupStore
        );
        
        //When
        SearchQuery newQuery = query.withFacetFilter(filter);
        
        //Then
        assertTrue("Expected query to contain filter", newQuery.containsFacetFilter(filter));
    }
    
    @Test
    public void checkThatWithoutFacetFilterRemovesFilter() {
        //Given
        FacetFilter filter = new FacetFilter("what", "ever");
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            18,
            DEFAULT_ROWS,
            Arrays.asList(filter),
            groupStore
        );
        
        //When
        SearchQuery newQuery = query.withoutFacetFilter(filter);
        
        //Then
        assertFalse("Expected query to not contain filter", newQuery.containsFacetFilter(filter));
    }
    
    @Test
    public void checkThatContainsFilterDelegatesToList() {
        //Given
        List<FacetFilter> filters = Arrays.asList(new FacetFilter("hey", "lo"));
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            filters,
            groupStore
        );
        
        FacetFilter filter = new FacetFilter("hey", "lo");
        
        //When
        query.containsFacetFilter(filter);
        
        //Then
        assertThat(filters.contains(filter), is(true));
    }
    
    @Test
    public void checkThatCompleteUrlIsGenerated() {
        //Given
        SearchQuery interestingQuery = new SearchQuery(
            "http://my.endpo.int",
            CatalogueUser.PUBLIC_USER,
            "My Search Term",
            "1,2,3,4",
            SpatialOperation.ISWITHIN,
            24,
            30,
            Arrays.asList(new FacetFilter("a","b")),
            groupStore
        );
        
        //When
        String url = interestingQuery.toUrl();
        
        //Then
        assertThat("Term should be searched for", url, containsString("term=My+Search+Term"));
        assertThat("BBOX should be searched for", url, containsString("bbox=1,2,3,4"));
        assertThat("OP should be present", url, containsString("op=IsWithin"));
        assertThat("page should be specified", url, containsString("page=24"));
        assertThat("rows should be present", url, containsString("rows=30"));
        assertThat("facet should be filtered", url, containsString("facet=a|b"));
        assertThat("endpoint should be defined ", url, startsWith("http://my.endpo.int?"));
    }
    
    @Test
    public void checkThatDefaultQueryDoesNotContainQueryString() {
        //Given
        SearchQuery boringQuery = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            DEFAULT_FILTERS,
            groupStore
        );
        
        //When
        String url = boringQuery.toUrl();
        
        //Then
        assertThat("Excepted url to be just endpoint", url, equalTo(ENDPOINT));
    }
    
    @Test
    public void changeInBBoxFilterReturnsANewSearchQuery() {
        //Given 
        String newBbox = "10,20,30,40";
        
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            DEFAULT_FILTERS,
            groupStore
        );
        
        //When
        SearchQuery newQuery = query.withBbox(newBbox);
        
        //Then
        assertNotSame("Expected the new query to differ from the last", newQuery, query);
    }
    
    @Test
    public void sameBBoxReturnsSameSearchQuery() {
        //Given        
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            DEFAULT_FILTERS,
            groupStore
        );
        
        //When
        SearchQuery newQuery = query.withBbox(DEFAULT_BBOX);
        
        //Then
        assertSame("Expected the new query to be exactly the same", newQuery, query);
    }
    
    @Test
    public void impFacetsConfigured() {
        //Given          
        SearchQuery query = new SearchQuery(
            ENDPOINT,
            CatalogueUser.PUBLIC_USER,
            SearchQuery.DEFAULT_SEARCH_TERM,
            DEFAULT_BBOX,
            SpatialOperation.ISWITHIN,
            DEFAULT_PAGE,
            DEFAULT_ROWS,
            Arrays.asList(new FacetFilter("repository","Catchment Management Platform")),
            groupStore
        );
        
        //When
        List<Facet> actual = query.getFacets();
        
        //Then
        assertThat("Should be 6 facets", actual.size(), is(6));
        assertThat("Second facet should be Broader Catachment issues", actual.get(1).getFieldName(), is("impBroaderCatchmentIssues"));
    }
}
