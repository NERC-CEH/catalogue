package uk.ac.ceh.gateway.catalogue.gemini;

import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Test;

public class BoundingBoxTest {

    @Test
    public void getGoogleStaticMapUrl() {
        //Given
        BoundingBox boundingBox = BoundingBox.builder()
            .westBoundLongitude("-1.3425")
            .eastBoundLongitude("2.3492")
            .southBoundLatitude("56.1234")
            .northBoundLatitude("57.0021")
            .build();
        
        String expected = "https://maps.googleapis.com/maps/api/staticmap?sensor=false&size=300x300&path=color:0xAA0000FF|weight:3|56.1234,-1.3425|56.1234,2.3492|57.0021,2.3492|57.0021,-1.3425|56.1234,-1.3425";
        
        //When
        String actual = boundingBox.getGoogleStaticMapUrl();
        
        //Then
        assertThat("GoogleStaticMapUrl 'actual' should be equal to 'expected'", actual, equalTo(expected));
    }
    
}
