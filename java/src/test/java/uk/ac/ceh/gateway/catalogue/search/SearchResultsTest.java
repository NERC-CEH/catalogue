package uk.ac.ceh.gateway.catalogue.search;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

public class SearchResultsTest {
    
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
            SearchQueryTest.DEFAULT_FILTERS);
        
        QueryResponse response = mock(QueryResponse.class);
        
        FacetField topic = new FacetField("topic");
        topic.add("0/Climate/", 5);
        topic.add("1/Climate/Climate change/", 2);
        topic.add("1/Climate/Meteorology/", 2);
        topic.add("2/Climate/Meteorology/Medium term forecast/", 1);
        topic.add("0/Modelling/", 8);
        topic.add("1/Modelling/Integrated ecosystem modelling/", 3);
        topic.add("0/Soil/", 5);
        
        FacetField resouceType = new FacetField("resouceType");
        resouceType.add("dataset", 3);
        resouceType.add("service", 12);
        
        given(response.getFacetField("topic")).willReturn(topic);
        given(response.getFacetField("resourceType")).willReturn(resouceType);
        
        List<Facet> expected = Arrays.asList(
            Facet.builder()
                .fieldName("topic")
                .displayName("Topic")
                .hierarchical(true)
                .results(Arrays.asList(
                    FacetResult.builder()
                        .name("Climate")
                        .url("http://catalogue.com/documents?facet=topic|0%2FClimate%2F")
                        .active(false)
                        .count(5)
                        .subFacetResults(Arrays.asList(
                            FacetResult.builder()
                                .name("Climate change")
                                .url("http://catalogue.com/documents?facet=topic|1%2FClimate%2FClimate+change%2F")
                                .active(false)
                                .count(2)
                                .build(),
                            FacetResult.builder()
                                .name("Meteorology")
                                .url("http://catalogue.com/documents?facet=topic|1%2FClimate%2FMeteorology%2F")
                                .active(false)
                                .count(2)
                                .subFacetResults(Arrays.asList(
                                    FacetResult.builder()
                                        .name("Medium term forecast")
                                        .url("http://catalogue.com/documents?facet=topic|2%2FClimate%2FMeteorology%2FMedium+term+forecast%2F")
                                        .active(false)
                                        .count(1)
                                        .build()
                                ))
                                .build()
                        ))
                        .build(),
                    FacetResult.builder()
                        .name("Modelling")
                        .url("http://catalogue.com/documents?facet=topic|0%2FModelling%2F")
                        .active(false)
                        .count(8)
                        .subFacetResults(Arrays.asList(
                            FacetResult.builder()
                                .name("Integrated ecosystem modelling")
                                .url("http://catalogue.com/documents?facet=topic|1%2FModelling%2FIntegrated+ecosystem+modelling%2F")
                                .active(false)
                                .count(3)
                                .build()
                        ))
                        .build(),
                    FacetResult.builder()
                        .name("Soil")
                        .url("http://catalogue.com/documents?facet=topic|0%2FSoil%2F")
                        .active(false)
                        .count(5)
                        .build()
                ))
                .build(),
            Facet.builder()
                .fieldName("resourceType")
                .displayName("Resource type")
                .results(Arrays.asList(
                    FacetResult.builder()
                        .name("dataset")
                        .url("http://catalogue.com/documents?facet=resouceType|dataset")
                        .active(false)
                        .count(3)
                        .build(),
                    FacetResult.builder()
                        .name("service")
                        .url("http://catalogue.com/documents?facet=resouceType|service")
                        .active(false)
                        .count(12)
                        .build()))
                .build(),
            Facet.builder()
                .fieldName("licence")
                .displayName("Licence")
                .build()
        );
                
        //When
        List<Facet> actual = new SearchResults(response, query).getFacets();
        
        //Then
       assertThat("Actual Facets should equal expected", actual, equalTo(expected)); 
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
            SpatialOperation.ISWITHIN,
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
            SpatialOperation.ISWITHIN,
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
            SpatialOperation.ISWITHIN,
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
            SpatialOperation.ISWITHIN,
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
            SearchQueryTest.DEFAULT_FILTERS);
        
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
            SearchQueryTest.DEFAULT_FILTERS);
        
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
            SearchQueryTest.DEFAULT_FILTERS);
        
        
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
            SearchQueryTest.DEFAULT_FILTERS);
        
        
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
            SearchQueryTest.DEFAULT_FILTERS);
        
        
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
            SearchQueryTest.DEFAULT_FILTERS);
        
        
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
            SearchQueryTest.DEFAULT_FILTERS);
        
        //When
        SearchQuery newQuery = query.withSpatialOperation(SpatialOperation.ISWITHIN);
        
        //Then
        assertThat("Isn't on page 400", newQuery.getPage(), not(equalTo(page)));
    }
}
