package uk.ac.ceh.gateway.catalogue.indexing;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import static org.hamcrest.CoreMatchers.is;

import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class MapFileTest {
    @Mock Configuration templateConfig;
    @Mock MetadataDocument document;
    
    @BeforeEach
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
        List<String> epsgCodes = Arrays.asList("Anything");
        MapFile mapfile = new MapFile(templateConfig, templateName, epsgCodes, document);
        
        //When
        mapfile.writeTo("27700", writer);
        
        //Then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(template).process(captor.capture(), eq(writer));
        assertThat(captor.getValue().get("epsgCode"), is("27700"));
        assertThat(captor.getValue().get("doc"), is(document));
    }
}
