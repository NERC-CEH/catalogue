package uk.ac.ceh.gateway.catalogue.geometry;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("MultiPolygonGeometry")
class MultiPolygonGeometryTest {

    @Test
    @DisplayName("multiPolygon has WKT representation")
    void multiPolygonHasWkt() {
        // Given
        val geometryString = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-4.0,55.0],[-3.0,54.0],[-2.0,53.0],[-4.0,55.0]]],[[[-6.0,50.0],[-5.0,49.0],[-7.0,51.0],[-6.0,50.0]]]]}}";
        val geometry = Geometry.builder().geometryString(geometryString).build();
        val expected = "MULTIPOLYGON(((-4.0 55.0, -3.0 54.0, -2.0 53.0, -4.0 55.0)), ((-6.0 50.0, -5.0 49.0, -7.0 51.0, -6.0 50.0)))";

        // When
        val actual = geometry.getWkt().get();

        // Then
        assertThat(actual, equalTo(expected));
    }

    @Test
    @DisplayName("multiPolygon has a bounding box")
    void multiPolygonHasBoundingBox() {
        // Given
        val geometryString = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-4.0,55.0],[-3.0,54.0],[-2.0,53.0],[-4.0,55.0]]],[[[-6.0,50.0],[-5.0,49.0],[-7.0,51.0],[-6.0,50.0]]]]}}";
        val geometry = Geometry.builder().geometryString(geometryString).build();
        val expected = "BoundingBox(westBoundLongitude=-7.0, eastBoundLongitude=-2.0, southBoundLatitude=49.0, northBoundLatitude=55.0)";

        // When
        val actual = geometry.getBoundingBox().get().toString();

        // Then
        assertThat(actual, equalTo(expected));
    }
}
