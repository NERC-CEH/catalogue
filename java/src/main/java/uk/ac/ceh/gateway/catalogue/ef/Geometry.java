package uk.ac.ceh.gateway.catalogue.ef;

import javax.xml.bind.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Geometry {
    @XmlAttribute(name = "SRS")
    private final String SRS = "urn:ogc:def:crs:EPSG::4326";
    @XmlAttribute
    private Boolean representativePoint;
    @XmlValue
    private String value;
}
