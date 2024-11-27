package uk.ac.ceh.gateway.catalogue.geometry;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("PointGeometry")
class PointGeometryTest {

    @Test
    @DisplayName("point has WKT representation")
    void pointHasWkt() {
        // Given
        val geometryString = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-1.535339,53.252069]}}";
        val geometry = Geometry.builder().geometryString(geometryString).build();
        val expected = "POINT(-1.535339 53.252069)";

        // When
        val actual = geometry.getWkt().get();

        // Then
        assertThat(actual, equalTo(expected));
    }

    @Test
    @DisplayName("point has a bounding box")
    void pointHasBoundingBox() {
        // Given
        val geometryString = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-1.12345,53.12345]}}";
        val geometry = Geometry.builder().geometryString(geometryString).build();
        val expected = "BoundingBox(westBoundLongitude=-1.12355, eastBoundLongitude=-1.12335, southBoundLatitude=53.123349999999995, northBoundLatitude=53.12355)";

        // When
        val actual = geometry.getBoundingBox().get().toString();

        // Then
        assertThat(actual, equalTo(expected));
    }
}
