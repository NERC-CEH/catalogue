package uk.ac.ceh.gateway.catalogue.indexing.mapserver;

import com.vividsolutions.jts.geom.Envelope;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StreamUtils;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition;
import uk.ac.ceh.gateway.catalogue.wms.MapServerDetailsService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.GeminiExtractor;

import java.io.File;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class MapFileTest {
    @Mock private GeminiExtractor geminiExtractor;
    @Mock private MapServerDetailsService mapServerDetailsService;

    private Configuration config;
    private GeminiDocument doc;
    private final String id = "908b8dc5-505a-4531-8a14-bd54bfee2417";

    @BeforeEach
    @SneakyThrows
    void setup() {
        doc = new GeminiDocument();
        doc.setId(id);
        doc.setUri("https://example.com/documents/" + id);
        doc.setTitle("Foo");
        val mapDataDefinition = new MapDataDefinition();
        /*
         * TODO: fill DataSource list to improve template testing
         * Need to expand DataSource to test template
         */
        mapDataDefinition.setData(Collections.emptyList());
        doc.setMapDataDefinition(mapDataDefinition);

        config = new Configuration(Configuration.VERSION_2_3_30);
        config.setDirectoryForTemplateLoading(new File("../templates"));
        config.setSharedVariable("geminiHelper", geminiExtractor);
        config.setSharedVariable("mapServerDetails", mapServerDetailsService);
    }

    @SneakyThrows
    void givenExtent() {
        given(geminiExtractor.getExtent(doc))
            .willReturn(new Envelope(20, 30, 40, 50));
    }

    void givenWmsUrl() {
        given(mapServerDetailsService.getWmsUrl(id))
            .willReturn("https://example.com/" + id);
    }

    @SneakyThrows
    private String expectedResponse(String filename) {
        return StreamUtils.copyToString(
            getClass().getResourceAsStream(filename),
            StandardCharsets.UTF_8
        );
    }

    @Test
    @SneakyThrows
    void writeMapFileToWriter() {
        //given
        val templateName = "mapfile/service.map.ftl";
        val epsgCodes = Arrays.asList("27700", "4326");
        val mapfile = new MapFile(config, templateName, epsgCodes, doc);
        val writer = new StringWriter();
        givenExtent();
        givenWmsUrl();
        val expected = expectedResponse("mapfile.map");

        //when
        mapfile.writeTo("27700", writer);

        //then
        assertThat(writer.toString(), equalToCompressingWhiteSpace(expected));
    }
}
