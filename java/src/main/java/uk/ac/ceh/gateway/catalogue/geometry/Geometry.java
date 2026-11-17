package uk.ac.ceh.gateway.catalogue.geometry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.Value;

import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;

@Value
@Slf4j
// Derived from geometryString on read: accepted and discarded on write, so a stored
// or client-supplied value can never disagree with the geometry itself.
@JsonIgnoreProperties(value = {"latitude", "longitude", "obfuscatedPoint"}, allowGetters = true)
public class Geometry {

    private static final String TYPE_POINT = "point";
    private static final String TYPE_POLYGON = "polygon";
    private static final String TYPE_MULTIPOLYGON = "multipolygon";

    String geometryString;
    Boolean locationConfidential;

    /**
     * The centre of the graticule cell a confidential location was reduced to.
     */
    public record ObfuscatedPoint(double latitude, double longitude) {}

    @JsonIgnore
    GeometryType geometryType;

    @Builder
    @JsonCreator
    private Geometry(
        @JsonProperty("geometryString") String geometryString,
        @JsonProperty("locationConfidential") Boolean locationConfidential
    ) {
        this.geometryString = nullToEmpty(geometryString);
        this.locationConfidential = locationConfidential;
        this.geometryType = parseGeometryType(this.geometryString);
    }

    /**
     * Determines the geometry type (Point, Polygon, MultiPolygon) based on the GeoJSON string.
     *
     * @param geometryString the raw GeoJSON string
     * @return the corresponding GeometryType implementation
     */
    private GeometryType parseGeometryType(String geometryString) {
        if (geometryString.isBlank()) {
            return null;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(geometryString);
            String type = root.at("/geometry/type").asString().toLowerCase();
            JsonNode coordinates = root.at("/geometry/coordinates");

            if (!coordinates.isArray()) {
                throw new IllegalArgumentException("Invalid geometry JSON structure");
            }

            switch (type) {
                case TYPE_POINT:
                    return new PointGeometry(coordinates);
                case TYPE_POLYGON:
                    return new PolygonGeometry(coordinates);
                case TYPE_MULTIPOLYGON:
                    return new MultiPolygonGeometry(coordinates);
                default:
                    String errorMessage = "There is not yet an implementation of getBoundingBox() for shapes of type: " + type;
                    log.error(errorMessage);
                    throw new UnsupportedOperationException(errorMessage);
            }
        } catch (UnsupportedOperationException e) {
            log.error("Unsupported geometry type: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error parsing geometry JSON: {}", e.getMessage());
            throw new RuntimeException("Error parsing geometry JSON", e);
        }
    }

    /**
     * Generates the Well-Known Text (WKT) representation of the geometry.
     *
     * @return an Optional containing the WKT string if the geometry is valid
     */
    public Optional<String> getWkt() {
        if (geometryType == null) {
            return Optional.empty();
        }
        return geometryType.getWkt();
    }

    /**
     * Calculates the bounding box for the geometry.
     *
     * @return an Optional containing the BoundingBox if the geometry is valid
     */
    @JsonIgnore
    public Optional<BoundingBox> getBoundingBox() {
        if (geometryType == null) {
            return Optional.empty();
        }
        return geometryType.getBoundingBox();
    }

    /**
     * @return the latitude of a point location, or null for any other shape. Also
     *         null for a confidential location, so a precise point stored before
     *         obfuscation ran on save is never published here.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Double getLatitude() {
        return publicPoint().map(PointGeometry::getLatitude).orElse(null);
    }

    /**
     * @return the longitude of a point location; null as for {@link #getLatitude()}
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Double getLongitude() {
        return publicPoint().map(PointGeometry::getLongitude).orElse(null);
    }

    /**
     * Computed rather than stored, so every confidential record has one without
     * being re-saved. Snapping is idempotent, so for a geometry already reduced on
     * save this is the centre of that same cell, and for one that never was it is
     * still only the centre of a cell.
     *
     * @return the centre of the cell a confidential location is published as, or
     *         null if the location is not confidential or has no extent
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ObfuscatedPoint getObfuscatedPoint() {
        if (!Boolean.TRUE.equals(locationConfidential)) {
            return null;
        }
        return getBoundingBox()
            .map(GraticuleCell::covering)
            .map(cell -> new ObfuscatedPoint(
                cell.centreLatitude().doubleValue(),
                cell.centreLongitude().doubleValue()
            ))
            .orElse(null);
    }

    private Optional<PointGeometry> publicPoint() {
        if (Boolean.TRUE.equals(locationConfidential) || !(geometryType instanceof PointGeometry point)) {
            return Optional.empty();
        }
        return Optional.of(point);
    }
}
