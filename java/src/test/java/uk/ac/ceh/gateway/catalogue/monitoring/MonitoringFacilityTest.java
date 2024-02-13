package uk.ac.ceh.gateway.catalogue.monitoring;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.gemini.Geometry;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("MonitoringFacility")
class MonitoringFacilityTest {

    @Test
    @DisplayName("has no geometry and boundingBox")
    void getEmptyWKTs() {
        //given
        val facility = new MonitoringFacility();
        val expected = Collections.emptyList();


        //when
        val actual = facility.getWKTs();

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    @DisplayName("has geometry and no boundingBox")
    void getWKTs() {
        //given
        val facility = new MonitoringFacility();
        val geometry = Geometry
            .builder()
            .geometryString("{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-1.53,53.25]}}")
            .build();
        facility.setGeometry(geometry);

        //when
        val actual = facility.getWKTs();

        //then
        assertThat(actual.size(), equalTo(1));
    }
}
