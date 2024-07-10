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

    public String getWkt() {
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

    /**
     * Tests to determine whether the incomming bounding box is completely within this bounding box
     * @param thatBox the bounding box to be compared against this one
     * @return if thatBox is completely inside this bounding box then 'true' else 'false'
     */
    public boolean isSurrounding(BoundingBox thatBox){
        return this.northBoundLatitude.compareTo(thatBox.northBoundLatitude) > 0
            && this.southBoundLatitude.compareTo(thatBox.southBoundLatitude) < 0
            && this.eastBoundLongitude.compareTo(thatBox.eastBoundLongitude) > 0
            && this.westBoundLongitude.compareTo(thatBox.westBoundLongitude) < 0;
    }

    /**
     * This will return the minimum bounding box from the intersection of the incomming one to this one
     * @param thatBox the incomming bounding box to compare against this one
     * @return the minimum bounding box that contains the incomming bounding box and this one
     */
    public BoundingBox intersectingBBox(BoundingBox thatBox){
        return BoundingBox.builder()
            .northBoundLatitude((this.northBoundLatitude.compareTo(thatBox.northBoundLatitude) > 0 ? this.northBoundLatitude : thatBox.northBoundLatitude) + "")
            .southBoundLatitude((this.southBoundLatitude.compareTo(thatBox.southBoundLatitude) < 0 ? this.southBoundLatitude : thatBox.southBoundLatitude) + "")
            .eastBoundLongitude((this.eastBoundLongitude.compareTo(thatBox.eastBoundLongitude) > 0 ? this.eastBoundLongitude : thatBox.eastBoundLongitude) + "")
            .westBoundLongitude((this.westBoundLongitude.compareTo(thatBox.westBoundLongitude) < 0 ? this.westBoundLongitude : thatBox.westBoundLongitude) + "")
            .build();
    }
}
