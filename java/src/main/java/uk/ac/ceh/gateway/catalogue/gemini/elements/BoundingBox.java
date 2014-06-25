package uk.ac.ceh.gateway.catalogue.gemini.elements;

import java.math.BigDecimal;
import lombok.Value;
import lombok.experimental.Builder;

@Value
public class BoundingBox {
    private static final String STATIC_MAP_BASE_URL = "https://maps.googleapis.com/maps/api/staticmap?sensor=false&size=300x300&path=color:0xAA0000FF|weight:3|";
    private final BigDecimal westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude;
    private final StringBuilder googleStaticMapUrl;
    
    @Builder
    private BoundingBox(String westBoundLongitude, String eastBoundLongitude, String southBoundLatitude, String northBoundLatitude) {
        this.westBoundLongitude = new BigDecimal(westBoundLongitude);
        this.eastBoundLongitude = new BigDecimal(eastBoundLongitude);
        this.southBoundLatitude = new BigDecimal(southBoundLatitude);
        this.northBoundLatitude = new BigDecimal(northBoundLatitude);
        googleStaticMapUrl= new StringBuilder(STATIC_MAP_BASE_URL);
    }

    String getGoogleStaticMapUrl() {
        return googleStaticMapUrl
            .append(southBoundLatitude)
            .append(",")
            .append(westBoundLongitude)
            .append("|")
            .append(southBoundLatitude)
            .append(",")
            .append(eastBoundLongitude)
            .append("|")
            .append(northBoundLatitude)
            .append(",")
            .append(eastBoundLongitude)
            .append("|")
            .append(northBoundLatitude)
            .append(",")
            .append(westBoundLongitude)
            .append("|")
            .append(southBoundLatitude)
            .append(",")
            .append(westBoundLongitude)
            .toString();
    }
}