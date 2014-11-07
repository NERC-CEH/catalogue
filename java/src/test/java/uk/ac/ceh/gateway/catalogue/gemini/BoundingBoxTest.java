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
    
}
