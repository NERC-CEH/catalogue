package uk.ac.ceh.gateway.catalogue.indexing;

import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.MapServerDetailsService;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MapServerIndexGeneratorTest {
    
    @Mock Configuration templateConfig;
    @Mock MapServerDetailsService mapServerDetailsService;
    @InjectMocks private MapServerIndexGenerator generator;
    
    @Test
    public void checkThatCanLocateTheMapServerServiceTemplate() {
        //Given
        MetadataDocument document = mock(GeminiDocument.class);
        when(mapServerDetailsService.isMapServiceHostable(document)).thenReturn(true);
        
        //When
        String templateName = generator.getMapFileTemplate(document);
        
        //Then
        assertThat(templateName, equalTo("mapfile/service.map.tpl"));   
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
