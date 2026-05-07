package uk.ac.ceh.gateway.catalogue.geometry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Geometry {
    private static final String TYPE_POINT = "point";
    private static final String TYPE_POLYGON = "polygon";
    private static final String TYPE_MULTIPOLYGON = "multipolygon";

    String geometryString;
    @JsonIgnore
    GeometryType geometryType;

    @Builder
    @JsonCreator
    private Geometry(@JsonProperty("geometryString") String geometryString) {
        this.geometryString = nullToEmpty(geometryString);
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
}
