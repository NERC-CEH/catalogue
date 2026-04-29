package uk.ac.ceh.gateway.catalogue.geometry;

import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class PolygonGeometry implements GeometryType {
    private final List<List<double[]>> coordinates;

    public PolygonGeometry(JsonNode coordinatesNode) {
        this.coordinates = new ArrayList<>();
        for (JsonNode ringNode : coordinatesNode) {
            List<double[]> ring = new ArrayList<>();
            for (JsonNode pointNode : ringNode) {
                double lon = pointNode.get(0).asDouble();
                double lat = pointNode.get(1).asDouble();
                ring.add(new double[]{lon, lat});
            }
            this.coordinates.add(ring);
        }
    }

    /**
     * This translates a geojson polygon to Well Known Text
     * It is assumed to be WGS84 lon lat
     *
     * @return Optional<String>: a WKT representation of the polygon
     */
    @Override
    public Optional<String> getWkt() {
        StringBuilder wktFeature = new StringBuilder("POLYGON(");
        for (List<double[]> ring : coordinates) {
            wktFeature.append("(");
            for (double[] point : ring) {
                wktFeature.append(point[0]).append(" ").append(point[1]).append(", ");
            }
            wktFeature.setLength(wktFeature.length() - 2); // Remove last comma and space
            wktFeature.append("), ");
        }
        wktFeature.setLength(wktFeature.length() - 2); // Remove last comma and space
        wktFeature.append(")");
        return Optional.of(wktFeature.toString());
    }

    /**
     * This gets the bounding box for a POLYGON returning the minimum rectangle that encompasses the points.
     *
     * @return BoundingBox
     */
    @Override
    public Optional<BoundingBox> getBoundingBox() {
        if (coordinates.isEmpty()) {
            return Optional.empty();
        }

        double north = Double.NEGATIVE_INFINITY;
        double south = Double.POSITIVE_INFINITY;
        double east = Double.NEGATIVE_INFINITY;
        double west = Double.POSITIVE_INFINITY;

        for (List<double[]> ring : coordinates) {
            for (double[] point : ring) {
                double lon = point[0];
                double lat = point[1];

                north = max(north, lat);
                south = min(south, lat);
                east = max(east, lon);
                west = min(west, lon);
            }
        }

        return Optional.of(
            BoundingBox.builder()
                .northBoundLatitude(Double.toString(north))
                .southBoundLatitude(Double.toString(south))
                .eastBoundLongitude(Double.toString(east))
                .westBoundLongitude(Double.toString(west))
                .build()
        );
    }
}
