package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;

import static com.google.common.base.Strings.nullToEmpty;

@Value
@Slf4j
public class Geometry {
    private final String geometryString;

    @Builder
    @JsonCreator
    private Geometry(@JsonProperty("geometryString") String value) {
        this.geometryString = nullToEmpty(value);
    }

    /**
     * This translates the geojson in the geometryString property to a Well Known Text string.
     *
     * Here is an example of the expected geojson for a polygon:
     * {"type":"Feature","properties":{},"geometryString":{"type":"Polygon","coordinates":[[[-4.570313,53.956086],[-4.570313,47.989922],[7.382813,47.754098],[-4.570313,53.956086]]]}}
     *
     * Here is an example of the expected geojson for a point:
     * {"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[-1.535339,53.252069]}}
     *
     * @return String: a Well Known Text representation of the geojson in the geometryString property
     */
    public String getWkt() {
        log.debug("Geometry object: {}", this.toString());
        ObjectMapper objectMapper = new ObjectMapper();
        if(this.getGeometryString().isEmpty() || this.getGeometryString().isBlank()){
            throw new RuntimeException("Geometry.getWkt(): geometryString is missing");
        }
        try {
            JsonNode root = objectMapper.readTree(this.getGeometryString());
            String type = root.at("/geometry/type").asText().toLowerCase();
            JsonNode coordinates = root.at("/geometry/coordinates");
            if(!coordinates.isArray()) {
                throw new RuntimeException("In Geometry.getWkt(), the expected array of coordinates in the property 'geometryString' was not found.");
            }
            String wkt = buildWkt(coordinates, type);
            log.debug("Well Known Text: {}", wkt);
            return wkt;
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
     * @coordinate JsonNode: the geojson polygon coorindates to be parsed, they are nested lat, lon coordinate arrays eg [[[-4.570313,53.956086],[-4.570313,47.989922],[7.382813,47.754098],[-4.570313,53.956086]]]
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
     * @coordinate JsonNode: the geojson point coorindate as a lon, lat array, eg [-1.535339,53.252069]}}
     **/
    private void addPoint(JsonNode coordinate, StringBuilder wktFeature){
        wktFeature.append("POINT(").append(coordinate.get(0)).append(" ").append(coordinate.get(1)).append(")");
    }

}
