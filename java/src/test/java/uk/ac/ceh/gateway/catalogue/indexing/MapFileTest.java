package uk.ac.ceh.gateway.catalogue.indexing;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.Writer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

/**
 *
 * @author cjohn
 */
public class MapFileTest {
    @Mock Configuration templateConfig;
    @Mock MetadataDocument document;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void checkThatCanWriteMapFileToWriter() throws Exception {
        //Given
        Writer writer = mock(Writer.class);
        Template template = mock(Template.class);
        String templateName = "mapserver.map.tpl";
        when(templateConfig.getTemplate(templateName)).thenReturn(template);
        MapFile mapfile = new MapFile(templateConfig, templateName, document);
        
        //When
        mapfile.writeTo(writer);
        
        //Then
        verify(template).process(document, writer);
    }
}
