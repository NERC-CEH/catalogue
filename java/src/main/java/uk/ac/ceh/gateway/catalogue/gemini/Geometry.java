package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.Value;

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

    public String getWkt() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String response = "";
            if( !(this.geometryString.isEmpty() || this.geometryString.isBlank()) ){
                System.out.println("geometrystring not blank");
                JsonNode root = objectMapper.readTree(this.getGeometryString());
                System.out.println(root.toString());
                String type = root.at("/geometryString/type").asText().toLowerCase();
                System.out.println("Geometry type: " + type);
                if(type.equals("polygon")){
                    this.getWKTPolygon(root.at("/geom/coordinates"));
                } else if (type.equals("point")) {
                    this.getWKTPoint();
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return "the getWkt() method had not been implemented yet, this is just filler text for the debugger";
    }

    private String getWKTPolygon(JsonNode coordinates){
        System.out.println("coordinates");
        System.out.println(coordinates.toString());
        if(coordinates.isArray()){
            JsonNode coords = coordinates.get(0).get(0);
            System.out.println("coords:");
            for (final JsonNode coordinate : coords){
                System.out.println(coordinate.toString());
            }
        }
        return "done";
    }

    private String getWKTPoint(){
        return "";
    }

//        return new StringBuilder()
//                .append("POLYGON((")
//                .append(westBoundLongitude).append(" ").append(southBoundLatitude).append(", ")
//                .append(westBoundLongitude).append(" ").append(northBoundLatitude).append(", ")
//                .append(eastBoundLongitude).append(" ").append(northBoundLatitude).append(", ")
//                .append(eastBoundLongitude).append(" ").append(southBoundLatitude).append(", ")
//                .append(westBoundLongitude).append(" ").append(southBoundLatitude)
//                .append("))")
//                .toString();
//    }
//
//        geojson
//    {"type":"Feature","properties":{},"geometryString":{"type":"Polygon","coordinates":[[[-4.570313,53.956086],[-4.570313,47.989922],[7.382813,47.754098],[-4.570313,53.956086]]]}}
//
//        wkt
//    POLYGON ((-4.570313 53.956086, -4.570313 47.989922, 7.382813 47.754098, -4.570313 53.956086))

}
