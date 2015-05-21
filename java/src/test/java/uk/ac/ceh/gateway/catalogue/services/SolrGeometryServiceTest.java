package uk.ac.ceh.gateway.catalogue.services;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;

public class SolrGeometryServiceTest {
    private SolrGeometryService service;
    
    @Before
    public void init() {
        service = new SolrGeometryService(new WKTReader());
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
        assertThat("Solr geometry produced", actual, equalTo("-1.3425 56.1234 2.3492 57.0021"));
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
}
