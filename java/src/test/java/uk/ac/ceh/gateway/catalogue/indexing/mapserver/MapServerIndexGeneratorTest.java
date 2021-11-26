package uk.ac.ceh.gateway.catalogue.indexing.mapserver;

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
import uk.ac.ceh.gateway.catalogue.wms.MapServerDetailsService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class MapServerIndexGeneratorTest {

    @Mock Configuration templateConfig;
    @Mock MapServerDetailsService mapServerDetailsService;
    @InjectMocks private MapServerIndexGenerator generator;

    private GeminiDocument doc;

    @BeforeEach
    void setup() {
        doc = new GeminiDocument();
    }

    private void givenMapDetails() {
        val mapDataDefinition = new MapDataDefinition();
        given(mapServerDetailsService.getMapDataDefinition(doc))
            .willReturn(mapDataDefinition);
        given(mapServerDetailsService.getProjectionSystems(mapDataDefinition))
            .willReturn(List.of("foo", "bar"));
    }

    @Test
    void generateIndex() {
        //given
        givenMapDetails();

        //when
        generator.generateIndex(doc);

        //then
        verifyNoInteractions(templateConfig);
    }
}
