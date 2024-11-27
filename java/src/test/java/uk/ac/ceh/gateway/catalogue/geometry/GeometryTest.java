package uk.ac.ceh.gateway.catalogue.geometry;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
