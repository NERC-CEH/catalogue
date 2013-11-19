package uk.ac.ceh.ukeof.model.simple;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

@Data
@Accessors(chain = true)
@XmlType(propOrder = {
    "metadata",
    "identifiers",
    "name",
    "alternativeNames",
    "description",
    "keywords",
    "geographicDescriptions",
    "boundingBoxes",
    "supplementalInfo",
    "responsibleParties",
    "funding",
    "operationCosts",
    "onlineResources",
    "objectives"
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
    private List<Keyword> keywords = new ArrayList<>();
    
    @XmlElement(name = "onlineResource")
    private List<Link> onlineResources  = new ArrayList<>();
    
    @XmlElement(name = "geographicDescription")
    private List<CodeList> geographicDescriptions  = new ArrayList<>();
    
    @XmlElement(name = "boundingBox")
    @Valid
    private List<BoundingBox> boundingBoxes  = new ArrayList<>();
    
    @XmlElement(name = "responsibleParty")
    List<ResponsibleParty> responsibleParties = new ArrayList<>();
    
    private Funding funding;
    
    private OperationCosts operationCosts;
    
    private List<String> supplementalInfo  = new ArrayList<>();
    
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
    public static class Keyword {
        @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
        private String href;
        @XmlAttribute
        private String thesaurusName, thesaurusDate;
        @XmlValue
        private String value;
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
}