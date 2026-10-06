package uk.ac.ceh.gateway.catalogue.geometry;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

@DisplayName("GraticuleCell")
class GraticuleCellTest {

    private static GraticuleCell at(String longitude, String latitude) {
        return GraticuleCell.containing(new BigDecimal(longitude), new BigDecimal(latitude));
    }

    @Test
    @DisplayName("truncates a positive coordinate down to the cell origin")
    void positiveCoordinate() {
        //given, when
        val cell = at("1.71792", "52.65757");

        //then
        assertThat(cell.minLongitude(), comparesEqualTo(new BigDecimal("1.7")));
        assertThat(cell.minLatitude(), comparesEqualTo(new BigDecimal("52.6")));
    }

    /**
     * Truncation must go towards negative infinity, not towards zero. Rounding
     * towards zero would fold the two cells either side of the meridian into one,
     * so a location at -1.53 would share a cell with one at 1.53.
     */
    @Test
    @DisplayName("truncates a negative coordinate towards negative infinity")
    void negativeCoordinate() {
        //given, when
        val cell = at("-1.53", "53.2507");

        //then
        assertThat(cell.minLongitude(), comparesEqualTo(new BigDecimal("-1.6")));
        assertThat(cell.minLatitude(), comparesEqualTo(new BigDecimal("53.2")));
    }

    @Test
    @DisplayName("a coordinate exactly on a boundary belongs to the cell it opens")
    void onBoundary() {
        //given, when
        val cell = at("1.7", "52.6");

        //then
        assertThat(cell.minLongitude(), comparesEqualTo(new BigDecimal("1.7")));
        assertThat(cell.minLatitude(), comparesEqualTo(new BigDecimal("52.6")));
    }

    /**
     * The property that makes publishing the cell safe: the cell is a function of
     * the grid, not of the location, so neighbouring locations are indistinguishable
     * once published.
     */
    @Test
    @DisplayName("neighbouring locations share one cell")
    void neighboursShareACell() {
        //given, when
        val one = at("1.71792", "52.65757");
        val other = at("1.79999", "52.60001");

        //then
        assertThat(one, equalTo(other));
    }

    /**
     * Snapping the centroid of a cell returns that same cell, so obfuscating an
     * already-obfuscated document is a no-op. This is what lets the published
     * GeoJSON carry no marker property - there is nothing to detect, because
     * re-running the transform cannot move the geometry.
     */
    @Test
    @DisplayName("snapping a cell's own centroid returns the same cell")
    void idempotent() {
        //given
        val cell = at("1.71792", "52.65757");

        //when
        val again = GraticuleCell.covering(cell.toBoundingBox());

        //then
        assertThat(again, equalTo(cell));
    }

    @Test
    @DisplayName("spans exactly one cell size from its origin")
    void extent() {
        //given, when
        val cell = at("1.71792", "52.65757");

        //then
        assertThat(cell.maxLongitude(), comparesEqualTo(new BigDecimal("1.8")));
        assertThat(cell.maxLatitude(), comparesEqualTo(new BigDecimal("52.7")));
    }

    @Test
    @DisplayName("renders a closed five-position ring")
    void geoJsonRing() {
        //given, when
        val geoJson = at("1.71792", "52.65757").toGeoJson();

        //then
        assertThat(geoJson, containsString("\"type\":\"Polygon\""));
        assertThat(geoJson, containsString("[[1.7,52.6],[1.8,52.6],[1.8,52.7],[1.7,52.7],[1.7,52.6]]"));
    }

    /**
     * Nothing in the published geometry may mark the record as confidential. A
     * lat/long aligned rectangle is the commonest shape in this catalogue - every
     * GEMINI record carries a bounding box - and it should stay unremarkable.
     */
    @Test
    @DisplayName("publishes no properties that would flag the record as sensitive")
    void geoJsonCarriesNoMarker() {
        //given, when
        val geoJson = at("1.71792", "52.65757").toGeoJson();

        //then
        assertThat(geoJson, containsString("\"properties\":{}"));
        assertThat(geoJson, not(containsString("onfidential")));
        assertThat(geoJson, not(containsString("ridReference")));
    }

    @Test
    @DisplayName("discards the precise coordinates it was derived from")
    void geoJsonHidesTheOriginal() {
        //given, when
        val geoJson = at("1.71792", "52.65757").toGeoJson();

        //then
        assertThat(geoJson, not(containsString("1.71792")));
        assertThat(geoJson, not(containsString("52.65757")));
    }
}
