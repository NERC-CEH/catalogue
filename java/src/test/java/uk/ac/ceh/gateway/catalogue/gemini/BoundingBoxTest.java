package uk.ac.ceh.gateway.catalogue.gemini;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Test;

public class BoundingBoxTest {

    @Test
    public void checkSolrGeometry() {
        //Given
        BoundingBox boundingBox = BoundingBox.builder()
            .westBoundLongitude("-1.3425")
            .eastBoundLongitude("2.3492")
            .southBoundLatitude("56.1234")
            .northBoundLatitude("57.0021")
            .build();
        
        
        //When
        String actual = boundingBox.getSolrGeometry();
        
        //Then
        assertThat("Solr geometry produced", actual, equalTo("-1.3425 56.1234 2.3492 57.0021"));
    }
    
    @Test
    public void checkWkt() {
        //Given
        BoundingBox boundingBox = BoundingBox.builder()
            .westBoundLongitude("-1.3425")
            .eastBoundLongitude("2.3492")
            .southBoundLatitude("56.1234")
            .northBoundLatitude("57.0021")
            .build();
        
        //When
        String actual = boundingBox.getWkt();
        
        //Then
        assertThat("WKT Created", actual, 
            equalTo("POLYGON((-1.3425 56.1234, -1.3425 57.0021, 2.3492 57.0021, 2.3492 56.1234, -1.3425 56.1234))"));
    }
    
}
