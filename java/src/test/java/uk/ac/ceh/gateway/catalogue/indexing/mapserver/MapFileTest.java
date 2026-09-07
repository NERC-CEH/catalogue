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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class MapFileTest {
    @Mock private GeminiExtractor geminiExtractor;
    private Configuration config;
    private GeminiDocument doc;
    private GeminiDocument doc2;

    @BeforeEach
    @SneakyThrows
    void setup() {
        doc = new GeminiDocument();
        doc2 = new GeminiDocument();

        String id = "908b8dc5-505a-4531-8a14-bd54bfee2417";
        doc.setId(id);
        doc.setUri("https://example.com/documents/" + id);
        doc.setTitle("Foo");

        doc2.setId(id);
        doc2.setUri("https://example.com/documents/" + id);
        doc2.setTitle("Foo");

        MapServerDetailsService mapServerDetailsService = new MapServerDetailsService(
            "https://example.com",
            "http://mapserver/mapserver/{id}"
        );
        val mapDataDefinition = new MapDataDefinition();

        MapDataDefinition.Projection projection = new MapDataDefinition.Projection();
        projection.setPath("/dummy/path");
        projection.setEpsgCode("EPSG:4326");

        MapDataDefinition.Style style = new MapDataDefinition.Style();
        style.setColour("#FF0000");
        style.setSymbol("circle");

        MapDataDefinition.DataSource.Attribute.Value value = new MapDataDefinition.DataSource.Attribute.Value();
        value.setStyle(style);
        value.setLabel("Value Label");
        value.setSetting("Setting1");

        MapDataDefinition.DataSource.Attribute.Bucket bucket = new MapDataDefinition.DataSource.Attribute.Bucket();
        bucket.setStyle(style);
        bucket.setLabel("Bucket Label");
        bucket.setMin(BigDecimal.valueOf(0));
        bucket.setMax(BigDecimal.valueOf(100));

        MapDataDefinition.DataSource.Attribute attribute = new MapDataDefinition.DataSource.Attribute();
        attribute.setName("Attribute1");
        attribute.setId("AttrID1");
        attribute.setLabel("Attribute Label");
        attribute.setType(MapDataDefinition.DataSource.AttributeType.TEXT);
        attribute.setValues(Collections.singletonList(value));
        attribute.setBuckets(Collections.singletonList(bucket));

        MapDataDefinition.DataSource dataSource1 = new MapDataDefinition.DataSource();
        dataSource1.setPath("/datasource/path1");
        dataSource1.setEpsgCode("EPSG:3857");
        dataSource1.setType("Vector");
        dataSource1.setLayer("Layer1");
        dataSource1.setReprojections(Collections.singletonList(projection));
        dataSource1.setAttributes(Collections.singletonList(attribute));
        dataSource1.setBytetype(true);

        MapDataDefinition.DataSource dataSource2 = new MapDataDefinition.DataSource();
        dataSource2.setPath("/datasource/path2");
        dataSource2.setEpsgCode("EPSG:32633");
        dataSource2.setType("Raster");
        dataSource2.setLayer("Layer2");
        dataSource2.setReprojections(Collections.singletonList(projection));
        dataSource2.setAttributes(Collections.singletonList(attribute));
        dataSource2.setBytetype(false);

        mapDataDefinition.setData(Collections.emptyList());
        doc.setMapDataDefinition(mapDataDefinition);

        MapDataDefinition mapDataDefinitionNonEmpty = new MapDataDefinition();
        mapDataDefinitionNonEmpty.setData(List.of(dataSource1, dataSource2));
        doc2.setMapDataDefinition(mapDataDefinitionNonEmpty);

        config = new Configuration(Configuration.VERSION_2_3_30);
        config.setDirectoryForTemplateLoading(new File("../templates"));
        config.setSharedVariable("geminiHelper", geminiExtractor);
        config.setSharedVariable("mapServerDetails", mapServerDetailsService);
    }

    @SneakyThrows
    void givenExtent(GeminiDocument doc) {
        given(geminiExtractor.getExtent(doc))
            .willReturn(new Envelope(20, 30, 40, 50));
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
        givenExtent(doc);
        val expected = expectedResponse("mapfile.map");

        //when
        mapfile.writeTo("27700", writer);
        //then
        assertThat(writer.toString(), equalToCompressingWhiteSpace(expected));
    }

    /** A test using a fully populate MapDataDefinition within the doc2 object in order to test all the mapfile templates
     * including different potential conditional outcomes within the templates **/
    @Test
    @SneakyThrows
    void writeMapFileToWriterFullyPopulatedMapDefinition() {
        //given
        val templateName = "mapfile/service.map.ftl";
        val epsgCodes = Arrays.asList("27700", "4326");
        val mapfile = new MapFile(config, templateName, epsgCodes, doc2);
        val writer = new StringWriter();
        givenExtent(doc2);
        val expected = expectedResponse("mapfile2.map");
        //when
        mapfile.writeTo("27700", writer);
        //then
        assertThat(writer.toString(), equalToCompressingWhiteSpace(expected));
    }

    /**
     * Without MAXSIZE, MapServer falls back to its own default of 4096, and a handful of
     * concurrent full-size renders exhausts the container's memory (dri-one #288).
     */
    @Test
    @SneakyThrows
    void mapFileCapsRequestedImageSize() {
        //given
        val mapfile = new MapFile(config, "mapfile/service.map.ftl", List.of("27700"), doc);
        val writer = new StringWriter();
        givenExtent(doc);

        //when
        mapfile.writeTo("27700", writer);

        //then
        assertThat(writer.toString(), containsString("MAXSIZE 2048"));
    }
}
