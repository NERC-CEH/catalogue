package uk.ac.ceh.gateway.catalogue.geometry;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public class PointGeometry implements GeometryType {
    private final double lon;
    private final double lat;
    public static final double POINT_PRECISION = 0.0001;

    public PointGeometry(JsonNode coordinates) {
        if (!coordinates.isArray() || coordinates.size() != 2) {
            throw new IllegalArgumentException("Invalid coordinates for Point");
        }
        this.lon = coordinates.get(0).asDouble();
        this.lat = coordinates.get(1).asDouble();
    }

    /**
     * This translates a simple geojson point to Well Known Text
     * It is assumed to be WGS84 lon lat
     *
     * @return Optional<String>: a WKT representation of the point
     **/
    @Override
    public Optional<String> getWkt() {
        String wkt = String.format("POINT(%s %s)", lon, lat);
        return Optional.of(wkt);
    }

    /**
     * This gets the bounding box for a POINT
     * A tiny bounding box is drawn around the point by adding and subtracting 0.0001
     * from the latituded and the longitude.  At the UK's latitude, this will lead to a box around 5 to 10m square.
     *
     * @return Optional<BoundingBox>
     */
    @Override
    public Optional<BoundingBox> getBoundingBox() {
        return Optional.of(
            BoundingBox.builder()
                .northBoundLatitude(Double.toString(lat + POINT_PRECISION))
                .southBoundLatitude(Double.toString(lat - POINT_PRECISION))
                .eastBoundLongitude(Double.toString(lon + POINT_PRECISION))
                .westBoundLongitude(Double.toString(lon - POINT_PRECISION))
                .build()
        );
    }
}
