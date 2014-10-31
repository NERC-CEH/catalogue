package uk.ac.ceh.gateway.catalogue.gemini;

import java.math.BigDecimal;
import lombok.Value;
import lombok.experimental.Builder;

@Value
public class BoundingBox {
    private final BigDecimal westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude;
    
    @Builder
    private BoundingBox(String westBoundLongitude, String eastBoundLongitude, String southBoundLatitude, String northBoundLatitude) {
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
}