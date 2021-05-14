package uk.ac.ceh.gateway.catalogue.indexing;

import freemarker.template.Configuration;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.MapServerDetailsService;

import java.util.Arrays;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class MapServerIndexGeneratorTest {

    @Mock Configuration templateConfig;
    @Mock MapServerDetailsService mapServerDetailsService;
    @InjectMocks private MapServerIndexGenerator generator;

    private MetadataDocument doc;

    @BeforeEach
    void setup() {
        doc = new GeminiDocument();
    }

    private void givenMapServiceIsHostable() {
        given(mapServerDetailsService.isMapServiceHostable(doc)).willReturn(true);
    }

    private void givenMapServiceIsNotHostable() {
        given(mapServerDetailsService.isMapServiceHostable(doc)).willReturn(false);
    }

    private void givenMapDetails() {
        val mapDataDefinition = new MapDataDefinition();
        given(mapServerDetailsService.getMapDataDefinition(doc))
            .willReturn(mapDataDefinition);
        given(mapServerDetailsService.getProjectionSystems(mapDataDefinition))
            .willReturn(Arrays.asList(
                "foo",
                "bar"
            ));
    }

    @Test
    void generateIndex() {
        //given
        givenMapServiceIsHostable();
        givenMapDetails();

        //when
        generator.generateIndex(doc);

        //then
        verifyNoInteractions(templateConfig);
    }

    @Test
    void noIndexGenerated() {
        //given
        givenMapServiceIsNotHostable();

        //when
        generator.generateIndex(doc);

        //then
        verifyNoInteractions(templateConfig);
        verifyNoMoreInteractions(mapServerDetailsService);
    }

//    @Test
//    public void checkThatCanLocateTheMapServerServiceTemplate() {
//        //Given
//        val document = new GeminiDocument();
//        given(mapServerDetailsService.isMapServiceHostable(document)).willReturn(true);
//
//        //When
//        String templateName = generator.getMapFileTemplate(document);
//
//        //Then
//        assertThat(templateName, equalTo("mapfile/service.map.ftl"));
//    }
//
//    @Test
//    public void checkThatUnknownDocumentReturnsNullTemplate() {
//        //Given
//        val document = new GeminiDocument();
//        given(mapServerDetailsService.isMapServiceHostable(document)).willReturn(false);
//
//        //When
//        String templateName = generator.getMapFileTemplate(document);
//
//        //Then
//        assertNull(templateName);
//    }
}
