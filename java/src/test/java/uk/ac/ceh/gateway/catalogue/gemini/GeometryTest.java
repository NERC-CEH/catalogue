package uk.ac.ceh.gateway.catalogue.gemini;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        val expected = "";

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
}
