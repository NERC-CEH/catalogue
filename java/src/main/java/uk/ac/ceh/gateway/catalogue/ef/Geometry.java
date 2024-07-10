package uk.ac.ceh.gateway.catalogue.ef;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;
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
