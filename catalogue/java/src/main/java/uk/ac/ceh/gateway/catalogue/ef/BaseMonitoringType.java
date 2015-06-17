package uk.ac.ceh.gateway.catalogue.ef;

import com.fasterxml.jackson.annotation.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;
import uk.ac.ceh.gateway.catalogue.ef.adapters.AnyXMLHandler;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

@Data
@Accessors(chain = true)
@XmlType(propOrder = {
    "efMetadata",
    "identifiers",
    "name",
    "alternativeNames",
    "description",
    "purposeOfCollection",
    "objectives",
    "keywords",
    "parametersMeasured",
    "boundingBoxes",
    "spatialResolution",
    "topicCategories",
    "environmentalDomains",
    "measurementRegime",
    "responsibleParties",
    "supplementalInfo",
    "onlineResources",
    "linkToData",
    "funding",
    "operationCosts",
    "codings",
    "useLimitations",
    "accessConstraints",
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
public class BaseMonitoringType implements MetadataDocument {
    @NotNull
    @Valid
    @XmlElement(name = "metadata")
    private Metadata efMetadata;
    
    @NotNull
    private String name;
    
    private String description, objectives;
    
    private Link measurementRegime;
    
    @XmlElement(name = "purposeOfCollection")
    private List<Link> purposeOfCollection = new ArrayList<>();
    
    @XmlElement(name = "identifier")
    private List<Identifier> identifiers  = new ArrayList<>();

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getId() {
        return efMetadata.getFileIdentifier().toString();
    }

    @Override
    public String getType() {
        return getClass().getSimpleName().toLowerCase();
    }

    @Override
    public List<String> getTopics() {
        return Collections.EMPTY_LIST;
    }
    
    @XmlTransient
    private MetadataInfo metadataInfo;
    
    @Override
    public MetadataInfo getMetadata() {
        return metadataInfo;
    }

    @Override
    public void attachMetadata(MetadataInfo metadataInfo) {
        this.metadataInfo = metadataInfo;
    }

    @Override
    public URI getUri() {
        try {
            return new URI(efMetadata.getSelfUrl());
        } catch (URISyntaxException ex) {
            return null;
        }
    }
    
    @Override
    public void attachUri(URI uri) {
        efMetadata.setSelfUrl(uri.toString());
    }
    
    @Data
    public static class Identifier {
        @XmlAttribute
        private String localIdentifier, namespace;
    }
   
    @XmlElement(name = "alternativeName")
    private List<String> alternativeNames  = new ArrayList<>();
    
    @XmlElement(name = "keyword")
    private List<Link> keywords = new ArrayList<>();
    
    private List<Link> parametersMeasured = new ArrayList<>();
    
    @XmlElement(name = "onlineResource")
    private List<Link> onlineResources  = new ArrayList<>();
    
    @XmlElement(name = "linkToData")
    private List<Link> linkToData = new ArrayList<>();
    
    @XmlElement(name = "boundingBox")
    @Valid
    private List<BoundingBox> boundingBoxes  = new ArrayList<>();
    
    private SpatialResolution spatialResolution;
    
    @XmlElement(name = "topicCategory")
    private List<Link> topicCategories = new ArrayList<>();
    
    @XmlElement(name = "environmentalDomain")
    private List<Link> environmentalDomains = new ArrayList<>();
    
    @XmlElement(name = "responsibleParty")
    List<ResponsibleParty> responsibleParties = new ArrayList<>();
    
    private Funding funding;
    
    private OperationCosts operationCosts;
    
    @XmlElement(name = "coding")
    private List<Coding> codings = new ArrayList<>();
    
    @XmlElement(name = "useLimitation")
    private List<Link> useLimitations = new ArrayList<>();
    
    @XmlElement(name = "accessConstraint")
    private List<Link> accessConstraints = new ArrayList<>();
    
    private List<Link> supplementalInfo  = new ArrayList<>();
    
    @XmlAnyElement(AnyXMLHandler.class)
    private String anyXML;
    
    @Data
    @JsonIgnoreProperties({"wkt"})
    @XmlType(propOrder = {"westBoundLongitude", "eastBoundLongitude", "southBoundLatitude", "northBoundLatitude" })
    public static class BoundingBox {
        @NotNull
        @Range(min = -180, max = 180)
        private BigDecimal westBoundLongitude, eastBoundLongitude;
        
        @NotNull
        @Range(min = -90, max = 90)
        private BigDecimal southBoundLatitude, northBoundLatitude;
        
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
    
    @Data
    public static class Coding {
        @XmlAttribute
        private String codeType;
        private List<String> code;
    }
}