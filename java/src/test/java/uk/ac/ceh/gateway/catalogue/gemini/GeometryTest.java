package uk.ac.ceh.gateway.catalogue.gemini;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Geometry")
class GeometryTest {

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
    @DisplayName("has point")
    void getPointWkt() {
        //given
        val geometryString = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-1.535339,53.252069]}}";
        val geometry = Geometry.builder().geometryString(geometryString).build();
        val expected = "POINT(-1.535339 53.252069)";

        //when
        val actual = geometry.getWkt().get();

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    @DisplayName("has polygon")
    void getPolygonWkt() {
        //given
        val geometryString = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[-4.570313,53.956086],[-4.570313,47.989922],[7.382813,47.754098],[-4.570313,53.956086]]]}}";
        val geometry = Geometry.builder().geometryString(geometryString).build();
        val expected = "POLYGON((-4.570313 53.956086, -4.570313 47.989922, 7.382813 47.754098, -4.570313 53.956086))";

        //when
        val actual = geometry.getWkt().get();

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    @DisplayName("point has a bounding box")
    void getBoundingBoxFromPoint() {
        //given
        val geometryString = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-1.12345,53.12345]}}";
        val geometry = Geometry.builder().geometryString(geometryString).build();
        val expected = "BoundingBox(westBoundLongitude=-1.12355, eastBoundLongitude=-1.12335, southBoundLatitude=53.123349999999995, northBoundLatitude=53.12355, extentName=, extentUri=)";

        //when
        val actual = geometry.getBoundingBox().get().toString();

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    @DisplayName("polygon has a bounding box")
    void getBoundingBoxFromPolygon(){
        //given
        val geometryString = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[1.0,2.0],[5.0,6.0],[8.0,9.0],[3.0,4.0],[-2.0,-4.0]]]}}";
        val geometry = Geometry.builder().geometryString(geometryString).build();
        val expected = "BoundingBox(westBoundLongitude=-2.0, eastBoundLongitude=8.0, southBoundLatitude=-4.0, northBoundLatitude=9.0, extentName=, extentUri=)";

        //when
        val actual = geometry.getBoundingBox().get().toString();

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    @DisplayName("wrong geometry type for bounding box")
    void wrongGeometryTypeForBoundingBox(){
        //given
        val geometryString = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Rectangle\",\"coordinates\":[[[1.0,2.0],[5.0,6.0],[8.0,9.0],[3.0,4.0],[-2.0,-4.0]]]}}";
        val geometry = Geometry.builder().geometryString(geometryString).build();
        val expected = Optional.empty();

        //when
        val actual = geometry.getBoundingBox();

        //then
        assertThat(actual, equalTo(expected));
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
}
