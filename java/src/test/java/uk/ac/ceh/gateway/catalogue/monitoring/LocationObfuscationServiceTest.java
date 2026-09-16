package uk.ac.ceh.gateway.catalogue.monitoring;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.geometry.Geometry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@DisplayName("Location obfuscation")
class LocationObfuscationServiceTest {

    private static final String PRECISE_POINT =
        "{\"type\":\"Feature\",\"properties\":{},\"geometry\":"
            + "{\"type\":\"Point\",\"coordinates\":[1.71792,52.65757]}}";

    /** A site boundary spanning several cells; only its centroid survives. */
    private static final String PRECISE_POLYGON =
        "{\"type\":\"Feature\",\"properties\":{},\"geometry\":"
            + "{\"type\":\"Polygon\",\"coordinates\":[[[1.61,52.61],[1.83,52.61],"
            + "[1.83,52.69],[1.61,52.69],[1.61,52.61]]]}}";

    private final LocationObfuscationService service = new LocationObfuscationService();

    private MonitoringFacility facilityWith(String geometryString, Boolean confidential) {
        val facility = new MonitoringFacility();
        facility.setGeometry(Geometry.builder()
            .geometryString(geometryString)
            .locationConfidential(confidential)
            .build());
        return facility;
    }

    @Test
    @DisplayName("replaces a confidential point with its graticule cell")
    void confidentialPointIsSnapped() {
        //given, when
        val facility = service.obfuscate(facilityWith(PRECISE_POINT, true));

        //then
        val actual = facility.getGeometry().getGeometryString();
        assertThat(actual, containsString("\"Polygon\""));
        assertThat(actual, containsString("[1.7,52.6]"));
        assertThat(actual, containsString("[1.8,52.7]"));
        assertThat(actual, not(containsString("1.71792")));
        assertThat(actual, not(containsString("52.65757")));
    }

    /**
     * The gap that the browser-side implementation never closed: ticking the box on
     * a polygon left the precise site boundary in place, while the page still
     * displayed a banner saying the position was illustrative.
     */
    @Test
    @DisplayName("reduces a confidential polygon to a single cell as well")
    void confidentialPolygonIsSnapped() {
        //given, when
        val facility = service.obfuscate(facilityWith(PRECISE_POLYGON, true));

        //then
        val actual = facility.getGeometry().getGeometryString();
        assertThat(actual, containsString("[1.7,52.6]"));
        assertThat(actual, not(containsString("1.61")));
        assertThat(actual, not(containsString("1.83")));
    }

    @Test
    @DisplayName("publishes nothing that marks the record as confidential")
    void publishedGeometryIsUnremarkable() {
        //given, when
        val facility = service.obfuscate(facilityWith(PRECISE_POINT, true));

        //then
        assertThat(facility.getGeometry().getGeometryString(), containsString("\"properties\":{}"));
    }

    @Test
    @DisplayName("keeps the confidentiality flag on the replacement")
    void flagSurvives() {
        //given, when
        val facility = service.obfuscate(facilityWith(PRECISE_POINT, true));

        //then
        assertThat(facility.getGeometry().getLocationConfidential(), is(equalTo(true)));
    }

    @Test
    @DisplayName("leaves a non-confidential point untouched")
    void nonConfidentialUntouched() {
        //given, when
        val facility = service.obfuscate(facilityWith(PRECISE_POINT, false));

        //then
        assertThat(facility.getGeometry().getGeometryString(), is(equalTo(PRECISE_POINT)));
    }

    @Test
    @DisplayName("leaves a point untouched when the flag is absent")
    void nullFlagUntouched() {
        //given, when
        val facility = service.obfuscate(facilityWith(PRECISE_POINT, null));

        //then
        assertThat(facility.getGeometry().getGeometryString(), is(equalTo(PRECISE_POINT)));
    }

    /**
     * Every save runs the transform, so obfuscating an already-obfuscated document
     * must not drift. This is also what makes a marker property in the published
     * GeoJSON unnecessary.
     */
    @Test
    @DisplayName("is idempotent - snapping an already-snapped cell changes nothing")
    void idempotent() {
        //given
        val once = service.obfuscate(facilityWith(PRECISE_POINT, true));
        val afterFirst = once.getGeometry().getGeometryString();

        //when
        val twice = service.obfuscate(once);

        //then
        assertThat(twice.getGeometry().getGeometryString(), is(equalTo(afterFirst)));
    }

    @Test
    @DisplayName("tolerates a facility with no geometry")
    void noGeometry() {
        //given
        val facility = new MonitoringFacility();

        //when
        service.obfuscate(facility);

        //then
        assertThat(facility.getGeometry(), is(nullValue()));
    }

    @Test
    @DisplayName("tolerates a confidential geometry with a blank geometry string")
    void blankGeometry() {
        //given, when
        val facility = service.obfuscate(facilityWith("", true));

        //then
        assertThat(facility.getGeometry().getGeometryString(), is(equalTo("")));
    }
}
