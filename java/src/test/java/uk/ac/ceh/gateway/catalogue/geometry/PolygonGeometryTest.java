package uk.ac.ceh.gateway.catalogue.geometry;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("PolygonGeometry")
class PolygonGeometryTest {

    @Test
    @DisplayName("polygon has WKT representation")
    void polygonHasWkt() {
        // Given
        val geometryString = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[-4.570313,53.956086],[-4.570313,47.989922],[7.382813,47.754098],[-4.570313,53.956086]]]}}";
        val geometry = Geometry.builder().geometryString(geometryString).build();
        val expected = "POLYGON((-4.570313 53.956086, -4.570313 47.989922, 7.382813 47.754098, -4.570313 53.956086))";

        // When
        val actual = geometry.getWkt().get();

        // Then
        assertThat(actual, equalTo(expected));
    }

    @Test
    @DisplayName("polygon has a bounding box")
    void polygonHasBoundingBox() {
        // Given
        val geometryString = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[1.0,2.0],[5.0,6.0],[8.0,9.0],[3.0,4.0],[-2.0,-4.0]]]}}";
        val geometry = Geometry.builder().geometryString(geometryString).build();
        val expected = "BoundingBox(westBoundLongitude=-2.0, eastBoundLongitude=8.0, southBoundLatitude=-4.0, northBoundLatitude=9.0)";

        // When
        val actual = geometry.getBoundingBox().get().toString();

        // Then
        assertThat(actual, equalTo(expected));
    }
}
