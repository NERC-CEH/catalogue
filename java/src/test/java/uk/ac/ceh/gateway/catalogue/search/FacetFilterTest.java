package uk.ac.ceh.gateway.catalogue.search;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


public class FacetFilterTest {

    @Test
    public void simpleToStringMethods() {
        //Given
        String field = "sci0";
        String value = "Land Cover";

        //When
        FacetFilter facetFilter = new FacetFilter(field, value);

        //Then
        assertThat("FacetFilter asURIContent should be 'sci0%7CLand+Cover'", facetFilter.asURIContent(), equalTo("sci0%7CLand+Cover"));
        assertThat("FacetFilter asSolrFilterQuery should be '{!term f=sci0}Land Cover'", facetFilter.asSolrFilterQuery(), equalTo("{!term f=sci0}Land Cover"));
    }

    @Test
    public void properlyEncodedToStringMethods() {
        //Given
        String value = "sci0|Monitoring & Observation Systems";

        //When
        FacetFilter facetFilter = new FacetFilter(value);

        //Then
        assertThat("FacetFilter asURIContent should be 'sci0%7CMonitoring+%26+Observation+Systems'", facetFilter.asURIContent(), equalTo("sci0%7CMonitoring+%26+Observation+Systems"));
        assertThat("FacetFilter asSolrFilterQuery should be '{!term f=sci0}Monitoring & Observation Systems'", facetFilter.asSolrFilterQuery(), equalTo("{!term f=sci0}Monitoring & Observation Systems"));
    }

    @Test
    public void createFacetFilterFromEncodedUrl() {
        //Given
        String value = "sci0%7CMonitoring+%26+Observation+Systems";

        //When
        FacetFilter facetFilter = new FacetFilter(value);

        //Then
        assertThat("FacetFilter field should be 'sci0'", facetFilter.getField(), equalTo("sci0"));
        assertThat("FacetFilter value should be 'Monitoring & Observation Systems'", facetFilter.getValue(), equalTo("Monitoring & Observation Systems"));
    }

}
