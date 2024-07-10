package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.Value;

import java.util.Arrays;
import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;

@Value
@Slf4j
public class Geometry {
    public static double POINT_PRECISION = 0.0001;  //At a latitude of 60, a precision of 0.00001 is 5.5m along longitude and 11.1m along latitude

    private static final String TYPE_POINT = "point";
    private static final String TYPE_POLYGON = "polygon";

    private final String geometryString;

    @Builder
    @JsonCreator
    private Geometry(@JsonProperty("geometryString") String geometryString) {
        this.geometryString = nullToEmpty(geometryString);
    }

    /**
     * This translates the geojson in the geometryString property to a Well Known Text string.
     * <p>
     * Here is an example of the expected geojson for a polygon:
     * {"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[-4.570313,53.956086],[-4.570313,47.989922],[7.382813,47.754098],[-4.570313,53.956086]]]}}
     * <p>
     * Here is an example of the expected geojson for a point:
     * {"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[-1.535339,53.252069]}}
     *
     * @return String: a Well Known Text representation of the geojson in the geometryString property
     */
    public Optional<String> getWkt() {
        log.debug("Geometry object: {}", this);
        if(this.getGeometryString().isEmpty() || this.getGeometryString().isBlank()){
            return Optional.empty();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(this.getGeometryString());
            String type = root.at("/geometry/type").asText().toLowerCase();
            JsonNode coordinates = root.at("/geometry/coordinates");
            if(!coordinates.isArray()) {
                throw new RuntimeException("In Geometry.getWkt(), the expected array of coordinates in the property 'geometry' was not found.");
            }
            String wkt = buildWkt(coordinates, type);
            log.debug("Well Known Text: {}", wkt);
            return Optional.of(wkt);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds the Well Known Text from the coordinates
     * @param coordinates JsonNode: Either a polygon or point JsonNode
     * @param type String: the type of the shape - either 'point' or 'polygon'
     * @return String: the incoming coordinates translated to Well Known Text
     */
    private String buildWkt(JsonNode coordinates, String type){
        if(!Arrays.asList("polygon","point").contains(type)){
            throw new RuntimeException("A polygon or point was not found in the geojson in Geometry.getWkt");
        }
        StringBuilder wktFeature = new StringBuilder();
        if(type.equals("polygon")){
            addPolygon(coordinates, wktFeature);
        } else {
            addPoint(coordinates, wktFeature);
        }
        return wktFeature.toString();
    }

    /**
     * This translates a simple geojson polygon to Well Known Text and adds it to the incoming wktFeature
     * It is assumed to be WGS84 lon lat
     * @param coordinates JsonNode: the geojson polygon coordinates to be parsed, they are nested lat, lon coordinate arrays eg [[[-4.570313,53.956086],[-4.570313,47.989922],[7.382813,47.754098],[-4.570313,53.956086]]]
     **/
    private void addPolygon(JsonNode coordinates, StringBuilder wktFeature){
        // Start the polygon
        wktFeature.append("POLYGON((");
        // Add the vertices
        for (final JsonNode coordinate : coordinates.get(0)){
            wktFeature.append(coordinate.get(0)).append(" ").append(coordinate.get(1)).append(", ");
        }
        // Close the polygon
        wktFeature.replace(wktFeature.length() - 2, wktFeature.length(), "") .append("))");
    }

    /**
     * This translates a simple geojson point to Well Known Text and adds it to the incoming wktFeature
     * It is assumed to be WGS84 lon lat
     * @param coordinate JsonNode: the geojson point coordinate as a lon, lat array, eg [-1.535339,53.252069]}}
     **/
    private void addPoint(JsonNode coordinate, StringBuilder wktFeature){
        wktFeature.append("POINT(").append(coordinate.get(0)).append(" ").append(coordinate.get(1)).append(")");
    }

    /**
     * This gets a bounding box for the geometry.
     * If the geometry is a POINT, then a tiny bounding box is drawn around the point by adding and subtracting 0.0001
     * from the latituded and the longitude.  At the UK's latitude, this will lead to a box around 5 to 10m square.
     * If the geometry is a POLYGON, then the minimum rectangle to encompass the points is returned.
     * If there is no geometry or the geometry is not a POINT or POLYGON, then an empty Optional is returned
     * @return an Optional<gemini.BoundingBox>
     */
    public Optional<BoundingBox> getBoundingBox() {
        if(this.getGeometryString().isEmpty() || this.getGeometryString().isBlank()){
            return Optional.empty();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(this.getGeometryString());
            String type = root.at("/geometry/type").asText().toLowerCase();
            JsonNode coordinates = root.at("/geometry/coordinates");
            if(!coordinates.isArray()) {
                throw new RuntimeException("In Geometry.getBoundingBox(), the expected array of coordinates in the property 'geometry' was not found.");
            }
            if(type.equals(this.TYPE_POINT)){
                return Optional.of(this.getPointBoundingBox(coordinates));
            }
            else if(type.equals(this.TYPE_POLYGON)){
                return Optional.of(this.getPolygonBoundingBox(coordinates));
            } else {
                throw new RuntimeException("There is not yet an implementation of getBoundingBox() for shapes of type: " + type);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This gets the bounding box for a POINT
     * A tiny bounding box is drawn around the point by adding and subtracting 0.0001
     * from the latituded and the longitude.  At the UK's latitude, this will lead to a box around 5 to 10m square.
     * @return BoundingBox
     */
    private BoundingBox getPointBoundingBox(JsonNode coordinates) {
        var lon = coordinates.get(0).asDouble();
        var lat = coordinates.get(1).asDouble();
        return BoundingBox.builder()
            .northBoundLatitude((lat + POINT_PRECISION)+"")
            .southBoundLatitude((lat - POINT_PRECISION)+"")
            .eastBoundLongitude((lon + POINT_PRECISION)+"")
            .westBoundLongitude((lon - POINT_PRECISION)+"")
            .build();
    }
    /**
     * This gets the bounding box for a POLYGON
     * If the geometry is a POLYGON, then the minimum rectangle that encompasses the points is returned.
     * @return BoundingBox
     */
    private BoundingBox getPolygonBoundingBox(JsonNode coordinates){
        double north = 0, south = 0, east = 0, west = 0;
        boolean first = true;
        for (final JsonNode coordinate : coordinates.get(0)) {
            double lon = coordinate.get(0).asDouble();
            double lat = coordinate.get(1).asDouble();
            if(first) {
                first = false;
                north = south = lat;
                east = west = lon;
            } else {
                north = (lat > north) ? lat : north;
                south = (lat < south) ? lat : south;
                east = (lon > east) ? lon : east;
                west = (lon < west) ? lon : west;
            }
        }
        return BoundingBox.builder()
            .northBoundLatitude(north+"")
            .southBoundLatitude(south+"")
            .eastBoundLongitude(east+"")
            .westBoundLongitude(west+"")
            .build();
    }
}
