package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Strings.nullToEmpty;
import java.math.BigDecimal;
import lombok.Value;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Value
@Slf4j
@JsonIgnoreProperties({"wkt"})
public class BoundingBox {
    private final BigDecimal westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude;
    private final String extentName, extentUri;

    @Builder
    @JsonCreator
    private BoundingBox(
            @JsonProperty("westBoundLongitude") String westBoundLongitude,
            @JsonProperty("eastBoundLongitude") String eastBoundLongitude,
            @JsonProperty("southBoundLatitude") String southBoundLatitude,
            @JsonProperty("northBoundLatitude") String northBoundLatitude,
            @JsonProperty("extentName") String extentName,
            @JsonProperty("extentUri") String extentUri) {
        log.debug("w: {}, e: {}, s: {}, n: {}", westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude);
        this.westBoundLongitude = new BigDecimal(westBoundLongitude);
        this.eastBoundLongitude = new BigDecimal(eastBoundLongitude);
        this.southBoundLatitude = new BigDecimal(southBoundLatitude);
        this.northBoundLatitude = new BigDecimal(northBoundLatitude);
        this.extentName = nullToEmpty(extentName);
        this.extentUri = nullToEmpty(extentUri);
    }

    public String getGeoJson() {
        return new StringBuilder()
                .append("POLYGON((")
                .append(westBoundLongitude).append(" ").append(southBoundLatitude).append(", ")
                .append(westBoundLongitude).append(" ").append(northBoundLatitude).append(", ")
                .append(eastBoundLongitude).append(" ").append(northBoundLatitude).append(", ")
                .append(eastBoundLongitude).append(" ").append(southBoundLatitude).append(", ")
                .append(westBoundLongitude).append(" ").append(southBoundLatitude)
                .append("))")
                .toString();
    }

    public String getCoordinates() {
        return new StringBuilder()
                .append("[[[")
                .append(westBoundLongitude).append(", ").append(southBoundLatitude).append("], [")
                .append(westBoundLongitude).append(", ").append(northBoundLatitude).append("], [")
                .append(eastBoundLongitude).append(", ").append(northBoundLatitude).append("], [")
                .append(eastBoundLongitude).append(", ").append(southBoundLatitude).append("], [")
                .append(westBoundLongitude).append(", ").append(southBoundLatitude)
                .append("]]]")
                .toString();
    }

    public String getBounds() {
        return new StringBuilder()
                .append("{\"type\": \"Feature\","+
                        "      \"properties\": {}," +
                        "      \"geometry\": {" +
                        "        \"type\": \"Polygon\"," +
                        "        \"coordinates\": " + getCoordinates() +
                        "      }}")
                .toString();
    }
}