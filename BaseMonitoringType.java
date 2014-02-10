package uk.ac.ceh.ukeof.model.simple;

import com.fasterxml.jackson.annotation.*;
import java.math.BigDecimal;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;
import uk.ac.ceh.ukeof.model.simple.adapters.AnyXMLHandler;

@Data
@Accessors(chain = true)
@XmlType(propOrder = {
    "metadata",
    "identifiers",
    "name",
    "alternativeNames",
    "description",
    "purposeOfCollection",
    "objectives",
    "keywords",
    "boundingBoxes",
    "spatialResolution",
    "environmentalDomains",
    "responsibleParties",
    "supplementalInfo",
    "onlineResources",
    "funding",
    "operationCosts",
    "anyXML"
})
@JsonIgnoreProperties(value = "id")
@JsonTypeInfo(  
    use = JsonTypeInfo.Id.NAME,  
    include = JsonTypeInfo.As.PROPERTY,  
    property = "type")  
@JsonSubTypes({  
    @JsonSubTypes.Type(value = Activity.class, name = "activity"),  
    @JsonSubTypes.Type(value = Programme.class, name = "programme"),
    @JsonSubTypes.Type(value = Network.class, name = "network"),
    @JsonSubTypes.Type(value = Facility.class, name = "facility")
})
@XmlSeeAlso({Activity.class, Programme.class, Network.class, Facility.class})
public class BaseMonitoringType {
    @NotNull
    @Valid
    private Metadata metadata;
    
    @NotNull
    private String name;
    
    private String description, objectives;
    
    @XmlElement(name = "purposeOfCollection")
    private List<Link> purposeOfCollection = new ArrayList<>();
    
    @XmlElement(name = "identifier")
    private List<Identifier> identifiers  = new ArrayList<>();
    
    @Data
    public static class Identifier {
        @XmlAttribute
        private String localIdentifier, namespace;
    }
   
    @XmlElement(name = "alternativeName")
    private List<String> alternativeNames  = new ArrayList<>();
    
    @XmlElement(name = "keyword")
    private List<Link> keywords = new ArrayList<>();
    
    @XmlElement(name = "onlineResource")
    private List<Link> onlineResources  = new ArrayList<>();
    
    @XmlElement(name = "boundingBox")
    @Valid
    private List<BoundingBox> boundingBoxes  = new ArrayList<>();
    
    private SpatialResolution spatialResolution;
    
    @XmlElement(name = "environmentalDomain")
    private List<Link> environmentalDomains = new ArrayList<>();
    
    @XmlElement(name = "responsibleParty")
    List<ResponsibleParty> responsibleParties = new ArrayList<>();
    
    private Funding funding;
    
    private OperationCosts operationCosts;
    
    private List<Link> supplementalInfo  = new ArrayList<>();
    
    @XmlAnyElement(AnyXMLHandler.class)
    private String anyXML;
    
    @Data
    @XmlType(propOrder = {"westBoundLongitude", "eastBoundLongitude", "southBoundLatitude", "northBoundLatitude" })
    public static class BoundingBox {
        @NotNull
        @Range(min = -180, max = 180)
        private BigDecimal westBoundLongitude, eastBoundLongitude;
        
        @NotNull
        @Range(min = -90, max = 90)
        private BigDecimal southBoundLatitude, northBoundLatitude;
        
        @Override
        public String toString() {
            return String.format("%s %s %s %s", westBoundLongitude, southBoundLatitude, eastBoundLongitude, northBoundLatitude);
        }
    }
        
    @Data
    @XmlType(propOrder = {
        "annualisedCost",
        "financialYearCost"
    })
    public static class OperationCosts {
        private AnnualisedCost annualisedCost;
        private List<FinancialYearCost> financialYearCost  = new ArrayList<>();

        @Data
        @XmlType(propOrder = {
            "cost",
            "inKindContributions",
            "costNotes"
        })
        public static class AnnualisedCost {
            private String cost, inKindContributions, costNotes;
        }

        @Data
        @XmlType(propOrder = {
            "cost",
            "inKindContributions",
            "costNotes",
            "year"
        })
        public static class FinancialYearCost {
            private String year, cost, inKindContributions, costNotes;
        }
    }
    
    @Data
    public static class SpatialResolution {
        @XmlAttribute
        private final String uom = "urn:ogc:def:uom:EPSG::9001";
        @XmlValue
        private BigDecimal value;
    }
}