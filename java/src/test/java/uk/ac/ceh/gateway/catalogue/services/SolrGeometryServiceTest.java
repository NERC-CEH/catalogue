package uk.ac.ceh.gateway.catalogue.services;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class SolrGeometryServiceTest {
    private SolrGeometryService service;
    
    @Before
    public void init() {
        service = new SolrGeometryService();
    }
    
    @Test
    public void checkBoundingBoxCanBeProcessed() {
        //Given
        BoundingBox boundingBox = BoundingBox.builder()
            .westBoundLongitude("-1.3425")
            .eastBoundLongitude("2.3492")
            .southBoundLatitude("56.1234")
            .northBoundLatitude("57.0021")
            .build();
        
        //When
        String actual = service.toSolrGeometry(boundingBox.getWkt());
        
        //Then
        assertThat("Solr geometry produced", actual, equalTo("ENVELOPE(-1.3425,2.3492,57.0021,56.1234)"));
    }
    
    @Test
    public void checkPointCanBeProcessed() {
        //Given
        String wkt = "POINT (12 30)";
        
        //When
        String actual = service.toSolrGeometry(wkt);
        
        //Then
        assertThat("Solr geometry produced", actual, equalTo("12.0 30.0"));
    }
    
    @Test
    public void emptyGeomReturnsNull() {
        //Given
        String wkt = "GEOMETRYCOLLECTION EMPTY";
        
        //When
        String actual = service.toSolrGeometry(wkt);
        
        //Then
        assertThat("Solr geometry produced", actual, equalTo(null));
    }
    
    @Test
    public void checkThatLineBoundingBoxCanBeProcessed() {
        //Given
        BoundingBox boundingBox = BoundingBox.builder()
            .westBoundLongitude("-1.33")
            .eastBoundLongitude("1.4")
            .southBoundLatitude("51.77")
            .northBoundLatitude("51.88")
            .build();
        
        //When
        String actual = service.toSolrGeometry(boundingBox.getWkt());
        
        //Then
        assertThat("Solr geometry produced", actual, equalTo("ENVELOPE(-1.33,1.4,51.88,51.77)"));
    }
}
