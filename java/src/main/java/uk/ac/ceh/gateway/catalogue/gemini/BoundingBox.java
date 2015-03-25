package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Value;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Value
@Slf4j
@JsonIgnoreProperties({"solrGeometry", "wkt"})
public class BoundingBox {
    private final BigDecimal westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude;
    
    @Builder
    @JsonCreator
    private BoundingBox(
        @JsonProperty("westBoundLongitude") String westBoundLongitude,
        @JsonProperty("eastBoundLongitude") String eastBoundLongitude,
        @JsonProperty("southBoundLatitude") String southBoundLatitude,
        @JsonProperty("northBoundLatitude") String northBoundLatitude) {
        log.debug("w: {}, e: {}, s: {}, n: {}", westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude);
        this.westBoundLongitude = new BigDecimal(westBoundLongitude);
        this.eastBoundLongitude = new BigDecimal(eastBoundLongitude);
        this.southBoundLatitude = new BigDecimal(southBoundLatitude);
        this.northBoundLatitude = new BigDecimal(northBoundLatitude);
    }
    
    public String getSolrGeometry() {
        return new StringBuilder()
                .append(westBoundLongitude)
                .append(" ")
                .append(southBoundLatitude)
                .append(" ")
                .append(eastBoundLongitude)
                .append(" ")
                .append(northBoundLatitude)
                .toString();
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
}