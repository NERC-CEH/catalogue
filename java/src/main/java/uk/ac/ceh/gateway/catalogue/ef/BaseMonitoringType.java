package uk.ac.ceh.gateway.catalogue.ef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;
import uk.ac.ceh.gateway.catalogue.ef.adapters.AnyXMLHandler;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.indexing.solr.GeoJson;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Relationship;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
@XmlType(propOrder = {
    "relationships",
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
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Activity.class, name = "activity"),
    @JsonSubTypes.Type(value = Programme.class, name = "programme"),
    @JsonSubTypes.Type(value = Network.class, name = "network"),
    @JsonSubTypes.Type(value = Facility.class, name = "facility")
})
@XmlSeeAlso({Activity.class, Programme.class, Network.class, Facility.class})
public class BaseMonitoringType implements MetadataDocument, GeoJson {

    private Set<Relationship> relationships;

    @NotNull
    @Valid
    @XmlElement(name = "metadata")
    private Metadata efMetadata;

    @NotNull
    private String name;

    private String description, objectives;

    private Link measurementRegime;

    @XmlTransient
    private LocalDateTime metadataDate;

    @Override
    @XmlTransient
    public String getMetadataDateTime() {
        return Optional.ofNullable(metadataDate)
            .map(md -> md.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .orElse("");
    }

    @XmlElement(name = "purposeOfCollection")
    private List<Link> purposeOfCollection = new ArrayList<>();

    @XmlElement(name = "identifier")
    private List<Identifier> identifiers  = new ArrayList<>();

    @XmlTransient
    private List<ResourceIdentifier> resourceIdentifiers;

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public MetadataDocument setTitle(String title) {
        setName(title);
        return this;
    }

    @Override
    public String getId() {
        return efMetadata.getFileIdentifier().toString();
    }

    @Override
    public BaseMonitoringType setId(String id) {
        efMetadata.setFileIdentifier(UUID.fromString(id));
        return this;
    }

    @Override
    public String getType() {
        return getClass().getSimpleName().toLowerCase();
    }

    @Override
    public MetadataDocument setType(String type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @XmlTransient
    private MetadataInfo metadata;

    @Override
    public String getUri() {
        return efMetadata.getSelfUrl();
    }

    @Override
    public BaseMonitoringType setUri(String uri) {
        efMetadata.setSelfUrl(uri);
        return this;
    }

    @Override
    @JsonIgnore
    public List<Keyword> getAllKeywords() {
        return keywords
            .stream()
            .map(Link::asKeyword)
            .collect(Collectors.toList());
    }

    @Override
    public BaseMonitoringType addAdditionalKeywords(List<Keyword> additionalKeywords) {
        keywords.addAll(
            additionalKeywords
                .stream()
                .map(Keyword::asLink)
                .collect(Collectors.toList())
        );
        return this;
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
            return "POLYGON((" +
                westBoundLongitude + " " + southBoundLatitude + ", " +
                westBoundLongitude + " " + northBoundLatitude + ", " +
                eastBoundLongitude + " " + northBoundLatitude + ", " +
                eastBoundLongitude + " " + southBoundLatitude + ", " +
                westBoundLongitude + " " + southBoundLatitude +
                "))";
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