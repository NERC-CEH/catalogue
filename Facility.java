package uk.ac.ceh.ukeof.model.simple;

import java.math.BigDecimal;
import java.util.*;
import javax.xml.bind.annotation.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.slf4j.*;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@XmlRootElement
@XmlType(propOrder = {
    "observingCapabilities",
    "supersedes",
    "supersededBy",
    "narrowerThan",
    "broaderThan",
    "involvedIn",
    "operationalPeriod",
    "measurementRegime",
    "resultAcquisitionSources",
    "mobile",
    "geometry",
    "belongsTo",
    "relatedTo"
})
public class Facility extends BaseMonitoringType {
    
    private List<Link> 
        supersedes  = new ArrayList<>(), 
        supersededBy = new ArrayList<>(),
        involvedIn = new ArrayList<>(),
        relatedTo = new ArrayList<>();
    
    private List<Lifespan> operationalPeriod;
    
    private List<Link.TimedLink>
        narrowerThan  = new ArrayList<>(),
        broaderThan = new ArrayList<>(),
        belongsTo = new ArrayList<>();
    
    @XmlElement(name = "observingCapability")
    private List<ObservingCapability> observingCapabilities  = new ArrayList<>();
    
    private Link measurementRegime;
    
    @XmlElement(name = "resultAcquisitionSource")
    private List<Link> resultAcquisitionSources  = new ArrayList<>();
    
    private String mobile;
    
    private Geometry geometry;
    
    @Data
    public static class Geometry {
        @XmlTransient
        private final StringBuilder googleStaticMapUrl 
            = new StringBuilder("http://maps.googleapis.com/maps/api/staticmap?sensor=false&size=300x300");
        
        @XmlTransient
        private final Logger logger = LoggerFactory.getLogger(Geometry.class);
        
        @XmlAttribute(name = "SRS")
        private final String SRS = "urn:ogc:def:crs:EPSG::4326";
        
        @XmlValue
        private String value;
        
        public String getStaticMapUrl() {
            
            if (value != null) {
                if (value.toLowerCase().indexOf("point") > -1) {
                    googleStaticMapUrl.append("&zoom=14&markers=");
                    int beginIndex = value.indexOf("(");
                    int endIndex = value.indexOf(" ", beginIndex);
                    String lonString = value.substring(beginIndex, endIndex);
                    logger.debug("lon string: {}", lonString);
                    BigDecimal lon = new BigDecimal(lonString);
                    beginIndex = endIndex;
                    endIndex = value.indexOf(")", beginIndex);
                    String latString = value.substring(beginIndex, endIndex);
                    logger.debug("lat string: {}", latString);
                    BigDecimal lat = new BigDecimal(latString);
                    googleStaticMapUrl.append(lat).append(",").append(lon);
                } else if (value.toLowerCase().indexOf("polygon") > -1) {
                    googleStaticMapUrl.append("&path=color:0xAA0000FF|weight:3");
                } else if (value.toLowerCase().indexOf("linestring") > -1) {
                    googleStaticMapUrl.append("&path=color:0xAA0000FF|weight:3");
                } else {
                    return "";
                }
            } else {
                
            }
            
            return googleStaticMapUrl.toString();
        }
    }
}