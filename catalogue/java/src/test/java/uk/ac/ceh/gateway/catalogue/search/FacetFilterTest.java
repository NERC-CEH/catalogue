package uk.ac.ceh.gateway.catalogue.search;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Test;

public class FacetFilterTest {

    @Test
    public void simpleToStringMethods() {
        //Given
        String field = "sci0";
        String value = "Land Cover";
        
        //When
        FacetFilter facetFilter = new FacetFilter(field, value);
        
        //Then
        assertThat("FacetFilter asFormContent should be 'sci0|Land Cover'", facetFilter.asFormContent(), equalTo("sci0|Land Cover"));
        assertThat("FacetFilter asURIContent should be 'sci0|Land+Cover'", facetFilter.asURIContent(), equalTo("sci0|Land+Cover"));
        assertThat("FacetFilter asSolrFilterQuery should be '{!term f=sci0}Land Cover'", facetFilter.asSolrFilterQuery(), equalTo("{!term f=sci0}Land Cover"));
    }
    
    @Test
    public void properlyEncodedToStringMethods() {
        //Given
        String value = "sci0|Monitoring & Observation Systems";
        
        //When
        FacetFilter facetFilter = new FacetFilter(value);
        
        //Then
        assertThat("FacetFilter asFormContent should be 'sci0|Monitoring & Observation Systems'", facetFilter.asFormContent(), equalTo("sci0|Monitoring & Observation Systems"));
        assertThat("FacetFilter asURIContent should be 'sci0|Monitoring+%26+Observation+Systems'", facetFilter.asURIContent(), equalTo("sci0|Monitoring+%26+Observation+Systems"));
        assertThat("FacetFilter asSolrFilterQuery should be '{!term f=sci0}Monitoring & Observation Systems'", facetFilter.asSolrFilterQuery(), equalTo("{!term f=sci0}Monitoring & Observation Systems"));
    }
    
}
