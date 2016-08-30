package uk.ac.ceh.gateway.catalogue.services;

import com.vividsolutions.jts.geom.Envelope;
import java.util.Arrays;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.imp.ImpDocument;

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
        GeminiDocument document = mock(GeminiDocument.class);
        BoundingBox bbox1 = BoundingBox.builder()
                .westBoundLongitude("1")
                .southBoundLatitude("2")
                .eastBoundLongitude("3")
                .northBoundLatitude("4")
                .build();
        when(document.getBoundingBoxes()).thenReturn(Arrays.asList(bbox1));
        
        //When
        Envelope env = service.getExtent(document);
        
        //Then
        assertThat(env.getMinX(), is(1d));
        assertThat(env.getMinY(), is(2d));
        assertThat(env.getMaxX(), is(3d));
        assertThat(env.getMaxY(), is(4d));
    }
    
    @Test
    public void checkThatCanGetContainedExtentOf2Bbox() throws Exception {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
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
        when(document.getBoundingBoxes()).thenReturn(Arrays.asList(bbox1, bbox2));
        
        //When
        Envelope env = service.getExtent(document);
        
        //Then
        assertThat(env.getMinX(), is(1d));
        assertThat(env.getMinY(), is(2d));
        assertThat(env.getMaxX(), is(7d));
        assertThat(env.getMaxY(), is(8d));
    }
}
