package uk.ac.ceh.gateway.catalogue.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;

/**
 *
 * @author cjohn
 */
public class MetadataInfoTest {
    @Test
    public void checkThatMetadataInfoCanParseMediaType() {
        //Given
        MediaType type = MediaType.IMAGE_JPEG;
        MetadataInfo info = new MetadataInfo();
        info.setRawType(type.toString());
        
        //When
        MediaType infoType = info.getRawMediaType();
        
        //Then
        assertEquals("Expected the mediatypes to be equal", type, infoType);
    }
    
    @Test
    public void canHideMediaTypeFromMetadataInfo() {
        //Given
        MetadataInfo info = new MetadataInfo();
        info.setRawType("application/xml");
        
        //When
        info.hideMediaType();
        
        //Then
        assertNull("Expected no media type to be specified", info.getRawType());
    }
}
