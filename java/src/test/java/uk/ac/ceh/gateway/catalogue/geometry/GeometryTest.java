package uk.ac.ceh.gateway.catalogue.geometry;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Geometry")
class GeometryTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private static final String POINT =
        "{\"type\":\"Feature\",\"properties\":{},\"geometry\":"
            + "{\"type\":\"Point\",\"coordinates\":[-2.7834,54.0123]}}";

    /** The cell LocationObfuscationService stores for POINT when it is confidential. */
    private static final String CELL =
        "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\","
            + "\"coordinates\":[[[-2.8,54.0],[-2.7,54.0],[-2.7,54.1],[-2.8,54.1],[-2.8,54.0]]]}}";

    private static JsonNode toJson(String geometryString, Boolean confidential) {
        return MAPPER.valueToTree(Geometry.builder()
            .geometryString(geometryString)
            .locationConfidential(confidential)
            .build());
    }

    @Test
    @DisplayName("has empty geometryString")
    void getEmptyWkt() {
        //given
        val geometry = Geometry.builder().geometryString("").build();

        //when
        val actual = geometry.getWkt().isEmpty();

        //then
        assertTrue(actual);
    }

    @Test
    @DisplayName("wrong geometry type for bounding box")
    void wrongGeometryTypeForBoundingBox() {
        // Given
        val geometryString = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Rectangle\",\"coordinates\":[[[1.0,2.0],[5.0,6.0],[8.0,9.0],[3.0,4.0],[-2.0,-4.0]]]}}";
        val expectedMessage = "There is not yet an implementation of getBoundingBox() for shapes of type: rectangle";

        // When & Then
        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> Geometry.builder().geometryString(geometryString).build()
        );

        // Verify
        assertThat(exception.getMessage(), equalTo(expectedMessage));
    }

    @Test
    @DisplayName("no geometry at all for bounding box")
    void noGeometryForBoundingBox(){
        //given
        val geometry = Geometry.builder().build();
        val expected = Optional.empty();

        //when
        val actual = geometry.getBoundingBox();

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    @DisplayName("publishes the latitude and longitude of a point")
    void pointPublishesLatLong() {
        //given, when
        val json = toJson(POINT, false);

        //then
        assertThat(json.get("latitude").asDouble(), equalTo(54.0123));
        assertThat(json.get("longitude").asDouble(), equalTo(-2.7834));
        assertThat(json.has("obfuscatedPoint"), is(false));
    }

    @Test
    @DisplayName("publishes no latitude or longitude for a polygon")
    void polygonHasNoLatLong() {
        //given, when
        val json = toJson(CELL, false);

        //then
        assertThat(json.has("latitude"), is(false));
        assertThat(json.has("longitude"), is(false));
    }

    @Test
    @DisplayName("publishes the centre of a confidential location's cell as the obfuscated point")
    void confidentialCellPublishesCentre() {
        //given, when
        val json = toJson(CELL, true);

        //then
        assertThat(json.at("/obfuscatedPoint/latitude").asDouble(), equalTo(54.05));
        assertThat(json.at("/obfuscatedPoint/longitude").asDouble(), equalTo(-2.75));
    }

    /**
     * A confidential point saved before obfuscation ran on save is still stored
     * precisely; neither new property may publish it.
     */
    @Test
    @DisplayName("never publishes the precise position of an unsnapped confidential point")
    void unsnappedConfidentialPointStaysHidden() {
        //given, when
        val json = toJson(POINT, true);

        //then
        assertThat(json.has("latitude"), is(false));
        assertThat(json.has("longitude"), is(false));
        assertThat(json.at("/obfuscatedPoint/latitude").asDouble(), equalTo(54.05));
        assertThat(json.at("/obfuscatedPoint/longitude").asDouble(), equalTo(-2.75));
    }

    @Test
    @DisplayName("has no obfuscated point for a confidential blank geometry")
    void confidentialBlankHasNoObfuscatedPoint() {
        //given
        val geometry = Geometry.builder().geometryString("").locationConfidential(true).build();

        //when
        val actual = geometry.getObfuscatedPoint();

        //then
        assertThat(actual, is(nullValue()));
    }

    /**
     * The editor PUTs back what it GETs, and records saved by MR !1371 hold a stored
     * obfuscatedPoint, so the derived properties must be accepted and ignored.
     */
    @Test
    @DisplayName("ignores supplied derived properties when reading")
    void derivedPropertiesIgnoredOnRead() {
        //given
        val json = "{\"geometryString\":\"\",\"locationConfidential\":true,"
            + "\"latitude\":1.0,\"longitude\":2.0,"
            + "\"obfuscatedPoint\":{\"latitude\":51.123456,\"longitude\":-1.654321}}";

        //when
        val geometry = MAPPER.readValue(json, Geometry.class);

        //then
        assertThat(geometry.getObfuscatedPoint(), is(nullValue()));
        assertThat(geometry.getLatitude(), is(nullValue()));
        assertThat(geometry.getLongitude(), is(nullValue()));
    }

    @Test
    @DisplayName("survives a JSON round trip")
    void roundTrip() {
        //given
        val original = Geometry.builder().geometryString(CELL).locationConfidential(true).build();

        //when
        val actual = MAPPER.readValue(MAPPER.writeValueAsString(original), Geometry.class);

        //then
        assertThat(actual.getGeometryString(), equalTo(CELL));
        assertThat(actual.getObfuscatedPoint(), equalTo(original.getObfuscatedPoint()));
    }
}
