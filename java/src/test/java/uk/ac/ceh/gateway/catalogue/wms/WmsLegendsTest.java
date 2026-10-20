package uk.ac.ceh.gateway.catalogue.wms;

import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.DataSource;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.DataSource.Attribute;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.DataSource.Attribute.Bucket;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.DataSource.Attribute.Value;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.DataSource.Features;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

class WmsLegendsTest {

    private static Style style(String colour) {
        Style style = new Style();
        style.setColour(colour);
        return style;
    }

    private static Value value(String label, String colour) {
        Value value = new Value();
        value.setLabel(label);
        value.setStyle(style(colour));
        return value;
    }

    private static Bucket bucket(String label, String colour) {
        Bucket bucket = new Bucket();
        bucket.setLabel(label);
        bucket.setStyle(style(colour));
        return bucket;
    }

    private static Attribute attribute(String name, List<Bucket> buckets, List<Value> values) {
        Attribute attribute = new Attribute();
        attribute.setName(name);
        attribute.setBuckets(buckets);
        attribute.setValues(values);
        return attribute;
    }

    private static DataSource source(String type, Attribute... attributes) {
        DataSource source = new DataSource();
        source.setType(type);
        source.setAttributes(List.of(attributes));
        return source;
    }

    private static MapDataDefinition definition(DataSource... sources) {
        MapDataDefinition definition = new MapDataDefinition();
        definition.setData(List.of(sources));
        return definition;
    }

    @Test
    void rasterAttributeIsALayerOfItsValuesInOrder() {
        Map<String, List<LegendEntry>> legends = WmsLegends.of(definition(source("RASTER",
            attribute("LC.25m.GB", null, List.of(
                value("Broadleaved, mixed & yew woodland", "#ff0000"),
                value("Coniferous woodland", "#006600"))))));

        assertThat(legends.get("LC.25m.GB"), contains(
            new LegendEntry("Broadleaved, mixed & yew woodland", "#ff0000", "area"),
            new LegendEntry("Coniferous woodland", "#006600", "area")));
    }

    @Test
    void bucketsComeBeforeValuesAsInTheMapfile() {
        Map<String, List<LegendEntry>> legends = WmsLegends.of(definition(source("POLYGON",
            attribute("A", List.of(bucket("Low", "#000001")), List.of(value("Exact", "#000002"))))));

        assertThat(legends.get("A"), contains(
            new LegendEntry("Low", "#000001", "area"),
            new LegendEntry("Exact", "#000002", "area")));
    }

    @Test
    void featuresAreALayerOfOneClass() {
        DataSource source = source("LINE");
        Features features = new Features();
        features.setName("rivers");
        features.setLabel("River");
        features.setStyle(style("#0000ff"));
        source.setFeatures(features);

        assertThat(WmsLegends.of(definition(source)).get("rivers"), contains(new LegendEntry("River", "#0000ff", "line")));
    }

    @Test
    void pointSourcesDrawPointSwatches() {
        assertThat(WmsLegends.of(definition(source("POINT", attribute("sites", null, List.of(value("Site", "#123456"))))))
            .get("sites"), contains(new LegendEntry("Site", "#123456", "point")));
    }

    @Test
    void layersKeepTheirDefinitionOrder() {
        Map<String, List<LegendEntry>> legends = WmsLegends.of(definition(
            source("RASTER", attribute("LC.25m.NI", null, List.of(value("a", "#000000")))),
            source("RASTER", attribute("LC.25m.GB", null, List.of(value("a", "#000000"))))));

        assertThat(new ArrayList<>(legends.keySet()), contains("LC.25m.NI", "LC.25m.GB"));
    }

    @Test
    void blankLabelsMissingStylesAndEmptyLayersAreTolerated() {
        Value unstyled = new Value();
        unstyled.setLabel(" Unstyled ");
        Map<String, List<LegendEntry>> legends = WmsLegends.of(definition(source(null,
            attribute("A", null, List.of(value(" ", "#000000"), unstyled)),
            attribute("Empty", List.of(), null),
            attribute(null, null, List.of(value("Nameless", "#000000"))))));

        assertThat(legends.get("A"), contains(new LegendEntry("Unstyled", null, "area")));
        assertThat(legends.containsKey("Empty"), is(false));
        assertThat(legends.size(), is(1));
    }

    @Test
    void noDefinitionMeansNoLegends() {
        assertThat(WmsLegends.of(null), is(anEmptyMap()));
        assertThat(WmsLegends.of(new MapDataDefinition()), is(anEmptyMap()));
    }
}
