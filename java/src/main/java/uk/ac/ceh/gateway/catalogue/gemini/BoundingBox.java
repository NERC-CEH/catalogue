package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import lombok.Value;
import lombok.experimental.Builder;
import lombok.extern.slf4j.Slf4j;

@Value
@Slf4j
@JsonIgnoreProperties({"solrGeometry", "wkt"})
public class BoundingBox {
    private final BigDecimal westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude;
    
    @Builder
    private BoundingBox(String westBoundLongitude, String eastBoundLongitude, String southBoundLatitude, String northBoundLatitude) {
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