package uk.ac.ceh.gateway.catalogue.indexing;

import freemarker.template.Configuration;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

/**
 *
 * @author cjohn
 */
public class MapServerIndexGeneratorTest {
    
    @Mock Configuration templateConfig;
    private MapServerIndexGenerator generator;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        generator = new MapServerIndexGenerator(templateConfig);
    }
    
    @Test
    public void checkThatCanLocateTheMapServerServiceTemplate() {
        //Given
        MetadataDocument document = mock(GeminiDocument.class);
        when(document.getType()).thenReturn("service");
        
        //When
        String templateName = generator.getMapFileTemplate(document);
        
        //Then
        assertThat(templateName, equalTo("mapfile/gemini-service.map.tpl"));   
    }
    
    @Test
    public void checkThatUnknownDocumentReturnsNullTemplate() {
        //Given
        MetadataDocument document = mock(MetadataDocument.class);
        
        //When
        String templateName = generator.getMapFileTemplate(document);
        
        //Then
        assertNull(templateName);   
    }
}
