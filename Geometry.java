package uk.ac.ceh.ukeof.model.simple;

import javax.xml.bind.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.slf4j.*;

@Data
@Accessors(chain = true)
public class Geometry {
    @XmlTransient
    private final StringBuilder googleStaticMapUrl 
        = new StringBuilder("http://maps.googleapis.com/maps/api/staticmap?sensor=false&size=300x300");
    private static final Logger logger = LoggerFactory.getLogger(Geometry.class);
    private static final int DECIMAL_PLACES = 3;
    
    @XmlAttribute(name = "SRS")
    private final String SRS = "urn:ogc:def:crs:EPSG::4326";
    @XmlValue
    private String value;

    public String getStaticMapUrl() {
        if (value != null) {
            if (value.toLowerCase().startsWith("point")) {
                googleStaticMapUrl.append("&zoom=14&markers=");
                
                int beginIndex = value.indexOf("(") + 1;
                int endIndex = value.indexOf(" ", beginIndex);
                String lon = fixedDecimal(value.substring(beginIndex, endIndex), DECIMAL_PLACES);
                
                beginIndex = endIndex + 1;
                endIndex = value.indexOf(")", beginIndex);
                String lat = fixedDecimal(value.substring(beginIndex, endIndex), DECIMAL_PLACES);

                googleStaticMapUrl.append(lat).append(",").append(lon);
            } else if (value.toLowerCase().startsWith("polygon")) {
                googleStaticMapUrl.append("&path=color:0xAA0000FF|weight:3");
            } else if (value.toLowerCase().startsWith("linestring")) {
                googleStaticMapUrl.append("&path=color:0xAA0000FF|weight:3");
            } else {
                return "";
            }
        } else {
        }
        return googleStaticMapUrl.toString();
    }
    
    private String fixedDecimal(String coord, int fixed) {
        int decimalIndex = coord.indexOf(".");
        if (decimalIndex > -1) {
            String preDecimal = coord.substring(0, decimalIndex);
            String postDecimal;
            try {
                postDecimal = coord.substring(decimalIndex, decimalIndex + fixed + 1);
            } catch (StringIndexOutOfBoundsException ex) {
                postDecimal = coord.substring(decimalIndex);
            }
            logger.debug("longitude: {}, preDecimal: {}, postDecimal: {}", coord, preDecimal, postDecimal);
            return new StringBuilder(preDecimal).append(postDecimal).toString();
        } else {
            logger.debug("no decimal point so just return input: {}", coord);
            return coord;
        }
    }
}