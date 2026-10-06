package uk.ac.ceh.gateway.catalogue.geometry;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * A fixed cell of the WGS84 graticule, used to publish a location coarsely enough
 * that the precise position is withheld.
 *
 * <p>The cell's boundaries come from the grid, not from the location inside it, so
 * its centroid discloses nothing beyond "somewhere in this cell". A buffer drawn
 * <em>around</em> a point does not have that property - the centroid of such a
 * buffer is the original point, however large the buffer is drawn.
 *
 * <p>At 0.1 degrees a cell is about 11.1km north to south, and between 5.4km and
 * 7.2km east to west across the United Kingdom - roughly 60 to 80 square
 * kilometres. Protection therefore varies a little with latitude, which is
 * inherent to any grid defined in degrees rather than metres.
 *
 * <p>Coordinates are truncated towards negative infinity rather than rounded, so
 * that cells tile continuously across the meridian and the equator, and so that
 * snapping an already-snapped cell is a no-op.
 */
public record GraticuleCell(BigDecimal minLongitude, BigDecimal minLatitude, BigDecimal sizeDegrees) {

    /**
     * The length of a cell's side, in degrees. Changing this changes how coarsely
     * confidential locations are published, and nothing else.
     */
    public static final BigDecimal SIZE_DEGREES = new BigDecimal("0.1");

    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    /**
     * @return the cell containing this position
     */
    public static GraticuleCell containing(BigDecimal longitude, BigDecimal latitude) {
        return new GraticuleCell(floorToCell(longitude), floorToCell(latitude), SIZE_DEGREES);
    }

    /**
     * @return the cell containing the centroid of this bounding box. A geometry
     *         larger than one cell therefore publishes as a single cell near its
     *         middle: the extent of the original is discarded along with its
     *         position, which is intended - a confidential polygon should not
     *         disclose the size or shape of the site either.
     */
    public static GraticuleCell covering(BoundingBox boundingBox) {
        return containing(
            midpoint(boundingBox.getWestBoundLongitude(), boundingBox.getEastBoundLongitude()),
            midpoint(boundingBox.getSouthBoundLatitude(), boundingBox.getNorthBoundLatitude())
        );
    }

    public BigDecimal maxLongitude() {
        return minLongitude.add(sizeDegrees);
    }

    public BigDecimal maxLatitude() {
        return minLatitude.add(sizeDegrees);
    }

    public BoundingBox toBoundingBox() {
        return BoundingBox.builder()
            .westBoundLongitude(minLongitude.toPlainString())
            .eastBoundLongitude(maxLongitude().toPlainString())
            .southBoundLatitude(minLatitude.toPlainString())
            .northBoundLatitude(maxLatitude().toPlainString())
            .build();
    }

    /**
     * Renders the cell as a GeoJSON Feature with an empty properties object.
     *
     * <p>Nothing here marks the record as confidential. Stamping a marker into the
     * published geometry would re-create the signal this whole approach exists to
     * avoid, and none is needed: the transform is idempotent by construction, so
     * there is no need to recognise a cell that has already been snapped.
     */
    public String toGeoJson() {
        String west = minLongitude.toPlainString();
        String east = maxLongitude().toPlainString();
        String south = minLatitude.toPlainString();
        String north = maxLatitude().toPlainString();

        return "{\"type\":\"Feature\",\"properties\":{},"
            + "\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[["
            + "[" + west + "," + south + "],"
            + "[" + east + "," + south + "],"
            + "[" + east + "," + north + "],"
            + "[" + west + "," + north + "],"
            + "[" + west + "," + south + "]"
            + "]]}}";
    }

    /**
     * {@code floor(value / size) * size} rather than {@code setScale(1, FLOOR)}, so
     * that changing {@link #SIZE_DEGREES} to 0.05 or 0.25 still tiles correctly.
     */
    private static BigDecimal floorToCell(BigDecimal value) {
        return value.divide(SIZE_DEGREES, 0, RoundingMode.FLOOR).multiply(SIZE_DEGREES);
    }

    private static BigDecimal midpoint(BigDecimal low, BigDecimal high) {
        return low.add(high).divide(TWO, MathContext.DECIMAL64);
    }
}
