package uk.ac.ceh.gateway.catalogue.gemini;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class BoundingBoxTest {
  
    @Test
    public void checkGeoJson() {
        //Given
        BoundingBox boundingBox = BoundingBox.builder()
            .westBoundLongitude("-1.3425")
            .eastBoundLongitude("2.3492")
            .southBoundLatitude("56.1234")
            .northBoundLatitude("57.0021")
            .build();
        
        //When
        String actual = boundingBox.getGeoJson();
        
        //Then
        assertThat("GeoJson Created", actual,
            equalTo("POLYGON((-1.3425 56.1234, -1.3425 57.0021, 2.3492 57.0021, 2.3492 56.1234, -1.3425 56.1234))"));
    }
    
}
