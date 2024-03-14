package uk.ac.ceh.gateway.catalogue.search;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class SearchQueryTest {
    static final String ENDPOINT = "http://catalogue.com/documents";
    static final String DEFAULT_BBOX = null;
    static final int DEFAULT_PAGE = 1;
    static final int DEFAULT_ROWS = 20;
    static final List<FacetFilter> DEFAULT_FILTERS = Collections.emptyList();
    @Mock private GroupStore<CatalogueUser> groupStore;
    private static final FacetFactory FACET_FACTORY = new HardcodedFacetFactory();
    static final List<Facet> DEFAULT_FACETS = FACET_FACTORY.newInstances(
            Arrays.asList("resourceType","licence")
            );

    @Test
    public void queryHasCatalogueAsViewFilter() {
        //Given
        SearchQuery query = new SearchQuery(
                ENDPOINT,
                new CatalogueUser("helen", "helen"),
                SearchQuery.DEFAULT_SEARCH_TERM,
                DEFAULT_BBOX,
                SpatialOperation.ISWITHIN,
                DEFAULT_PAGE,
                DEFAULT_ROWS,
                DEFAULT_FILTERS,
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );

        //When
        SolrQuery solrQuery = query.build();

        //Then
        assertThat(Arrays.asList(solrQuery.getFilterQueries()).contains("{!term f=catalogue}eidc"), is(true));

    }

    @Test
    public void loggedInUserHasUsernameAsViewFilter() {
        //Given
        CatalogueUser user = new CatalogueUser("helen", "helen");
        SearchQuery query = new SearchQuery(
                ENDPOINT,
                user,
                SearchQuery.DEFAULT_SEARCH_TERM,
                DEFAULT_BBOX,
                SpatialOperation.ISWITHIN,
                DEFAULT_PAGE,
                DEFAULT_ROWS,
                DEFAULT_FILTERS,
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );

        //When
        SolrQuery solrQuery = query.build();

        //Then
        assertThat(Arrays.asList(solrQuery.getFilterQueries()).contains("view:(public OR helen)"), is(true));
    }

    @Test
    public void loggedInUserWithGroupsHasUsernameAndGroupsAsViewFilter() {
        //Given
        CatalogueUser user = new CatalogueUser("helen", "helen");
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
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );

        //When
        SolrQuery solrQuery = query.build();

        //Then
        assertThat(Arrays.asList(solrQuery.getFilterQueries()).contains("view:(public OR helen OR ceh OR eidc)"), is(true));
    }

    @Test
    public void publisherDoesNotHaveViewFilter() {
        //Given
        CatalogueUser user = new CatalogueUser("publisher", "publisher");
        given(groupStore.getGroups(user)).willReturn(List.of(createGroup("ROLE_EIDC_PUBLISHER")));

        SearchQuery query = new SearchQuery(
                ENDPOINT,
                user,
                SearchQuery.DEFAULT_SEARCH_TERM,
                DEFAULT_BBOX,
                SpatialOperation.ISWITHIN,
                DEFAULT_PAGE,
                DEFAULT_ROWS,
                DEFAULT_FILTERS,
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );

        //When
        SolrQuery solrQuery = query.build();

        //Then
        assertThat("Solr query should have view filter", not(Arrays.toString(solrQuery.getFilterQueries()).contains("view:(public OR publisher OR role_cig_publisher)")));
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
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );
        //When
        SolrQuery solrQuery = query.build();

        //Then
        assertThat("Solr query should be the 'default text'", solrQuery.getQuery(), equalTo(SearchQuery.DEFAULT_SEARCH_TERM));
        assertThat(Arrays.asList(solrQuery.getFilterQueries()).contains("{!term f=view}public"), is(true));
        assertThat(Arrays.asList(solrQuery.getFilterQueries()).contains("{!term f=state}published"),is(true));
        assertThat(Arrays.asList(solrQuery.getFacetFields()).contains("licence"), is(true));
        assertThat(Arrays.asList(solrQuery.getFacetFields()).contains("resourceType"), is(true));
        assertThat(solrQuery.getStart(), is(equalTo(0)));
        assertThat(solrQuery.getRows(), is(equalTo(DEFAULT_ROWS)));
        assertThat(solrQuery.getFacetMinCount(), is(equalTo(1)));
        assertThat(solrQuery.getSorts().get(0).getItem().substring(0, 6), is(equalTo("random")));
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
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
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
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );
        //When
        SolrQuery solrQuery = query.build();

        //Then
        assertThat("Solr query should be the default text", solrQuery.getQuery(), equalTo(term));
        assertTrue(solrQuery.getSorts().isEmpty());
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
                List.of(
                    new FacetFilter("resourceType", "dataset")),
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );
        //When
        SolrQuery solrQuery = query.build();

        //Then
        assertThat(solrQuery.getQuery(), equalTo(SearchQuery.DEFAULT_SEARCH_TERM));
        assertTrue(List.of(solrQuery.getFilterQueries()).contains("{!term f=resourceType}dataset"));
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
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );

        //When
        SolrQuery solrQuery = query.build();

        //Then
        assertThat(Arrays.asList(solrQuery.getFilterQueries()).contains("locations:\"iswithin(ENVELOPE(1.11,2.22,3.33,4.44))\""), is(true));
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
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );

        //When
        SolrQuery solrQuery = query.build();

        //Then
        assertThat(Arrays.asList(solrQuery.getFilterQueries()).contains("locations:\"intersects(ENVELOPE(1.11,2.22,3.33,4.44))\""), is(true));
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
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );

        //When
        SearchQuery queryWithFacet = query.withFacetFilter(new FacetFilter("what", "ever"));

        //Then
        assertThat("Expected to be back on first page", queryWithFacet.getPage(), equalTo(1));
    }


    @Test
    public void checkThatWithoutFacetReturnsToFirstPage() {
        //Given
        FacetFilter filter = new FacetFilter("licence", "ever");
        SearchQuery query = new SearchQuery(
                ENDPOINT,
                CatalogueUser.PUBLIC_USER,
                SearchQuery.DEFAULT_SEARCH_TERM,
                DEFAULT_BBOX,
                SpatialOperation.ISWITHIN,
                18,
                DEFAULT_ROWS,
                List.of(filter),
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );

        //When
        SearchQuery queryWithFacet = query.withoutFacetFilter(filter);

        //Then
        assertThat("Expected to be back on first page", queryWithFacet.getPage(), equalTo(1));
    }

    @Test
    public void checkThatWithFacetFilterAddsNewFilter() {
        //Given
        FacetFilter filter = new FacetFilter("licence", "open");
        SearchQuery query = new SearchQuery(
                ENDPOINT,
                CatalogueUser.PUBLIC_USER,
                SearchQuery.DEFAULT_SEARCH_TERM,
                DEFAULT_BBOX,
                SpatialOperation.ISWITHIN,
                18,
                DEFAULT_ROWS,
                DEFAULT_FILTERS,
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );

        //When
        SearchQuery newQuery = query.withFacetFilter(filter);

        //Then
        assertTrue(newQuery.containsFacetFilter(filter));
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
                List.of(filter),
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );

        //When
        SearchQuery newQuery = query.withoutFacetFilter(filter);

        //Then
        assertFalse(newQuery.containsFacetFilter(filter));
    }

    @Test
    public void checkThatContainsFilterDelegatesToList() {
        //Given
        List<FacetFilter> filters = List.of(new FacetFilter("hey", "lo"));
        SearchQuery query = new SearchQuery(
                ENDPOINT,
                CatalogueUser.PUBLIC_USER,
                SearchQuery.DEFAULT_SEARCH_TERM,
                DEFAULT_BBOX,
                SpatialOperation.ISWITHIN,
                DEFAULT_PAGE,
                DEFAULT_ROWS,
                filters,
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
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
                List.of(new FacetFilter("licence", "b")),
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );

        //When
        String url = interestingQuery.toUrl();

        //Then
        assertThat("Term should be searched for", url, containsString("term=My+Search+Term"));
        assertThat("BBOX should be searched for", url, containsString("bbox=1,2,3,4"));
        assertThat("OP should be present", url, containsString("op=iswithin"));
        assertThat("page should be specified", url, containsString("page=24"));
        assertThat("rows should be present", url, containsString("rows=30"));
        assertThat("facet should be filtered", url, containsString("facet=licence%7Cb"));
        assertThat("endpoint should be defined ", url, startsWith("http://my.endpo.int?"));
    }

    @Test
    public void checkUrlIsGenerated() {
        //Given
        SearchQuery interestingQuery = new SearchQuery(
                "http://my.endpo.int",
                CatalogueUser.PUBLIC_USER,
                "My Search Term",
                "1,2,3,4",
                SpatialOperation.ISWITHIN,
                24,
                30,
                List.of(new FacetFilter("licence%7Cb")),
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );

        //When
        String url = interestingQuery.toUrl();

        //Then
        assertThat(url, equalTo("http://my.endpo.int?#page=24&rows=30&term=My+Search+Term&bbox=1,2,3,4&op=iswithin&facet=licence%7Cb"));
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
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
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
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );

        //When
        SearchQuery newQuery = query.withBbox(newBbox);

        //Then
        assertNotSame(newQuery, query);
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
                groupStore,
                Catalogue
                .builder()
                .id("eidc")
                .title("Environmental Information Data Centre")
                .url("https://eidc-catalogue.ceh.ac.uk")
                .contactUrl("")
                .logo("")
                .build(),
                DEFAULT_FACETS
                );

        //When
        SearchQuery newQuery = query.withBbox(DEFAULT_BBOX);

        //Then
        assertSame(newQuery, query);
    }

    @Test
    public void impFacetsConfigured() {
        //Given
        Catalogue catalogue = Catalogue.builder()
            .id("cmp")
            .title("Catchment Management Modelling Platform")
            .url("http://www.ceh.ac.uk")
            .contactUrl("")
            .logo("eidc.png")
            .facetKey("impCaMMPIssues")
            .facetKey("impDataType")
            .facetKey("impScale")
            .facetKey("impTopic")
            .facetKey("impWaterPollutant")
            .facetKey("resourceType")
            .build();
        SearchQuery query = new SearchQuery(
                ENDPOINT,
                CatalogueUser.PUBLIC_USER,
                SearchQuery.DEFAULT_SEARCH_TERM,
                DEFAULT_BBOX,
                SpatialOperation.ISWITHIN,
                DEFAULT_PAGE,
                DEFAULT_ROWS,
                DEFAULT_FILTERS,
                groupStore,
                catalogue,
                FACET_FACTORY.newInstances(catalogue.getFacetKeys())
                );

        //When
        List<String> actual = query
            .getFacets()
            .stream()
            .map(Facet::getFieldName)
            .collect(Collectors.toList());


        //Then
        assertThat(actual.contains("impCaMMPIssues"), is(true));
        assertThat(actual.contains("impDataType"), is(true));
        assertThat(actual.contains("impScale"), is(true));
        assertThat(actual.contains("impTopic"), is(true));
        assertThat(actual.contains("impWaterPollutant"), is(true));
        assertThat(actual.contains("resourceType"), is(true));

    }
}
