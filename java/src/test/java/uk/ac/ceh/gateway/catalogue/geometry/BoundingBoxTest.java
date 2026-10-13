package uk.ac.ceh.gateway.catalogue.geometry;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import lombok.val;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BoundingBoxTest {

    @Test
    public void checkWkt() {
        //Given
        BoundingBox boundingBox = BoundingBox.builder()
            .westBoundLongitude("-1.3425")
            .eastBoundLongitude("2.3492")
            .southBoundLatitude("56.1234")
            .northBoundLatitude("57.0021")
            .build();

        //When
        String actual = boundingBox.getWkt();

        //Then
        assertThat("WKT Created", actual,
            equalTo("POLYGON((-1.3425 56.1234, -1.3425 57.0021, 2.3492 57.0021, 2.3492 56.1234, -1.3425 56.1234))"));
    }

    @Test
    public void refusesAnIncompleteBoxWithAClearMessage() {
        // dri-one #457: a cleared or half-filled box from the editor arrived as nulls and failed
        // with a NullPointerException from new BigDecimal(null)
        assertThatThrownBy(() -> BoundingBox.builder().westBoundLongitude("-1").eastBoundLongitude("2").build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("A bounding box needs all four coordinates: west, east, south and north (missing south, north)");
    }

    @Test
    public void refusesAnIncompleteBoxInARequestBody() {
        val mapper = JsonMapper.builder().build();

        assertThatThrownBy(() -> mapper.readValue("{\"westBoundLongitude\":null,\"eastBoundLongitude\":\"2\"}", BoundingBox.class))
            .hasMessageContaining("A bounding box needs all four coordinates");
    }
}

