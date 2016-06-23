package uk.ac.ceh.gateway.catalogue.services;

import com.vividsolutions.jts.geom.Envelope;
import java.util.Arrays;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;

/**
 *
 * @author cjohn
 */
public class GeminiExtractorServiceTest {
    private GeminiExtractorService service;
    
    @Before
    public void init() {
        service = new GeminiExtractorService();
    }
    
    @Test
    public void checkThatCanGetContainedExtent() throws Exception {
        //Given
        BoundingBox bbox1 = BoundingBox.builder()
                .westBoundLongitude("1")
                .southBoundLatitude("2")
                .eastBoundLongitude("3")
                .northBoundLatitude("4")
                .build();
        
        //When
        Envelope env = service.getExtent(Arrays.asList(bbox1));
        
        //Then
        assertThat(env.getMinX(), is(1d));
        assertThat(env.getMinY(), is(2d));
        assertThat(env.getMaxX(), is(3d));
        assertThat(env.getMaxY(), is(4d));
    }
    
    @Test
    public void checkThatCanGetContainedExtentOf2Bbox() throws Exception {
        //Given
        BoundingBox bbox1 = BoundingBox.builder()
                .westBoundLongitude("1")
                .southBoundLatitude("2")
                .eastBoundLongitude("3")
                .northBoundLatitude("4")
                .build();
        
        BoundingBox bbox2 = BoundingBox.builder()
                .westBoundLongitude("5")
                .southBoundLatitude("6")
                .eastBoundLongitude("7")
                .northBoundLatitude("8")
                .build();
        
        //When
        Envelope env = service.getExtent(Arrays.asList(bbox1, bbox2));
        
        //Then
        assertThat(env.getMinX(), is(1d));
        assertThat(env.getMinY(), is(2d));
        assertThat(env.getMaxX(), is(7d));
        assertThat(env.getMaxY(), is(8d));
    }
}
