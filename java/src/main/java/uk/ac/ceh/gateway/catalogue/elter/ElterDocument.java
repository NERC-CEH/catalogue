package uk.ac.ceh.gateway.catalogue.elter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.citation.Citation;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.deims.DeimsSolrIndex;
import uk.ac.ceh.gateway.catalogue.gemini.*;
import uk.ac.ceh.gateway.catalogue.indexing.solr.GeoJson;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.util.*;
import java.util.stream.Collectors;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.*;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.WMS_GET_CAPABILITIES;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
        @Template(called = "html/elter.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE),
        @Template(called = "xml/elter.ftlx", whenRequestedAs = GEMINI_XML_VALUE),
        @Template(called = "rdf/ttl.ftlh", whenRequestedAs = RDF_TTL_VALUE),
        @Template(called = "schema.org/schema.org.ftlh", whenRequestedAs = RDF_SCHEMAORG_VALUE)
})
public class ElterDocument extends AbstractMetadataDocument implements GeoJson {
    private static final Set<String> ALLOWED_CITATION_FUNCTIONS = Set.of("isReferencedBy", "isSupplementTo");
    private static final String TOPIC_PROJECT_URL = "http://onto.nerc.ac.uk/CEHMD/";
    private String otherCitationDetails, lineage, reasonChanged,
            metadataStandardName, metadataStandardVersion;
    private List<String> alternateTitles, spatialRepresentationTypes, datasetLanguages,
            securityConstraints;
    private List<Keyword> topicCategories;
    private List<DistributionInfo> distributionFormats;
    private List<DescriptiveKeywords> descriptiveKeywords;
    private List<InspireTheme> inspireThemes;
    private List<SpatialResolution> spatialResolutions;
    private List<Funding> funding;
    private List<BoundingBox> boundingBoxes;
    private List<ResponsibleParty> responsibleParties;
    private List<TimePeriod> temporalExtents;
    private List<OnlineResource> onlineResources;
    private Set<Link> incomingRelationships;
    private List<SpatialReferenceSystem> spatialReferenceSystems;
    private List<Supplemental> supplemental;
    private List<RelatedRecord> relatedRecords;
    @JsonIgnore
    private Citation citation;
    @JsonIgnore
    private boolean isDataciteMintable;
    @JsonIgnore
    private boolean isDatacitable;
    private DatasetReferenceDate datasetReferenceDate;
    private Service service;
    private List<ResourceConstraint> useConstraints;
    private MapDataDefinition mapDataDefinition;
    private Keyword resourceType;
    private AccessLimitation accessLimitation;
    private boolean notGEMINI;
    private List<DeimsSolrIndex> deimsSites;
    private boolean linkedDocument;
    private String linkedDocumentUri;
    private String linkedDocumentType;


    @Override
    public String getType() {
        return Optional.ofNullable(resourceType)
                .map(Keyword::getValue)
                .orElse("");
    }

    public String getResourceStatus() {
        return Optional.ofNullable(accessLimitation)
                .map(AccessLimitation::getCode)
                .orElse(null);
    }

    @Override
    public ElterDocument setType(String type) {
        super.setType(type);
        this.resourceType = Keyword.builder().value(type).build();
        return this;
    }

    @Override
    @JsonIgnore
    public List<Keyword> getAllKeywords() {
        return Optional.ofNullable(descriptiveKeywords)
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(dk -> dk.getKeywords().stream())
                .collect(Collectors.toList());
    }

    @Override
    public ElterDocument addAdditionalKeywords(List<Keyword> additionalKeywords) {
        descriptiveKeywords = Optional.ofNullable(descriptiveKeywords)
                .orElse(new ArrayList<>());

        descriptiveKeywords.add(
                DescriptiveKeywords
                        .builder()
                        .keywords(additionalKeywords)
                        .build()
        );
        return this;
    }

    @JsonProperty("citation")
    public Citation getCitation() {
        return citation;
    }

    @JsonIgnore
    public ElterDocument setCitation(Citation citation) {
        this.citation = citation;
        return this;
    }

    public Set<Link> getAssociatedResources() {
        Set<Link> toReturn = new HashSet<>();
        if (incomingRelationships != null) {
            toReturn.addAll(incomingRelationships);
        }
        return toReturn;
    }

    /**
     * Return a link to the map viewer for this Gemini record if it can be
     * rendered in the map viewer
     *
     * @return The link to the map viewer if it is viewable else null
     */
    public String getMapViewerUrl() {
        if (isMapViewable()) {
            return "/maps#layers/" + getId();
        }
        return null;
    }

    /**
     * Decide if this gemini document has a map viewing capability. That is at
     * least one wms is registered to this gemini document
     *
     * @return true if a wms exists in the online resources
     */
    public boolean isMapViewable() {
        return Optional.ofNullable(onlineResources)
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch((o) -> WMS_GET_CAPABILITIES == o.getType());
    }

    public List<String> getTopics() {
        return Optional.ofNullable(descriptiveKeywords)
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(dk -> dk.getKeywords().stream())
                .filter(k -> k.getUri().startsWith(TOPIC_PROJECT_URL))
                .map(Keyword::getUri)
                .collect(Collectors.toList());
    }

    public List<String> getCoupledResources() {
        return Optional.ofNullable(service)
                .map(Service::getCoupledResources)
                .orElse(Collections.emptyList())
                .stream()
                .map(Service.CoupledResource::getIdentifier)
                .filter(cr -> !cr.isEmpty())
                .collect(Collectors.toList());
    }

    public List<ResponsibleParty> getResponsibleParties() {
        return Optional.ofNullable(responsibleParties)
                .orElse(Collections.emptyList());
    }

    public List<ResponsibleParty> getAuthors() {
        return Optional.ofNullable(responsibleParties)
                .orElse(Collections.emptyList())
                .stream()
                .filter((authors) -> authors.getRole().equalsIgnoreCase("author"))
                .collect(Collectors.toList());
    }

    public List<ResponsibleParty> getRightsHolders() {
        return Optional.ofNullable(responsibleParties)
                .orElse(Collections.emptyList())
                .stream()
                .filter((authors) -> authors.getRole().equalsIgnoreCase("rightsHolder"))
                .collect(Collectors.toList());
    }

    public List<Funding> getFunding() {
        return Optional.ofNullable(funding)
                .orElse(Collections.emptyList());
    }

    public List<Supplemental> getSupplemental() {
        return Optional.ofNullable(supplemental)
                .orElse(Collections.emptyList());
    }

    @Override
    public @NonNull List<String> getGeoJson() {
        return Optional.ofNullable(boundingBoxes)
                .orElse(Collections.emptyList())
                .stream()
                .map(BoundingBox::getGeoJson)
                .collect(Collectors.toList());
    }

    public long getIncomingCitationCount() {
        return Optional.ofNullable(supplemental)
                .orElse(Collections.emptyList())
                .stream()
                .filter(s -> ALLOWED_CITATION_FUNCTIONS.contains(s.getFunction()))
                .count();
    }

}
