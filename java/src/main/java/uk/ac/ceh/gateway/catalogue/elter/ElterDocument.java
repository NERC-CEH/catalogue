package uk.ac.ceh.gateway.catalogue.elter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.citation.Citation;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.deims.DeimsSolrIndex;
import uk.ac.ceh.gateway.catalogue.gemini.*;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class ElterDocument extends AbstractMetadataDocument implements WellKnownText {
    private static final Set<String> ALLOWED_CITATION_FUNCTIONS = Set.of("isReferencedBy", "isSupplementTo");
    private static final String TOPIC_PROJECT_URL = "http://onto.nerc.ac.uk/CEHMD/";
    private String otherCitationDetails, lineage, reasonChanged, metadataStandardName, metadataStandardVersion, dataLevel;
    private List<String> alternateTitles, spatialRepresentationTypes, datasetLanguages, securityConstraints;
    private List<Keyword> topicCategories, elterProject;
    private List<DistributionInfo> distributionFormats;
    private List<InspireTheme> inspireThemes;
    private List<SpatialResolution> spatialResolutions;
    private List<Funding> funding;
    private List<BoundingBox> boundingBoxes;
    private List<ResponsibleParty> responsibleParties, distributorContacts;
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
    private List<DeimsSolrIndex> deimsSites;
    private boolean linkedDocument;
    private String linkedDocumentUri;
    private String linkedDocumentType;

    @Override
    public Set<Relationship> getRelationships() {
        val relations = Optional.ofNullable(super.getRelationships())
            .orElse(Collections.emptySet());
        val related = Optional.ofNullable(relatedRecords)
            .orElse(Collections.emptyList())
            .stream()
            .map(RelatedRecord::toRelationship)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
        return Stream.of(relations, related)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public String getType() {
        return Optional.ofNullable(resourceType)
                .map(Keyword::getValue)
                .orElse("");
    }

    public String getResourceStatus() {
        return Optional.ofNullable(accessLimitation)
                .map(AccessLimitation::getCode)
                .filter(code -> !code.isEmpty())
                .orElse("Unknown");
    }

    @Override
    public ElterDocument setType(String type) {
        super.setType(type);
        this.resourceType = Keyword.builder().value(type).build();
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

    public List<String> getCoupledResources() {
        return Optional.ofNullable(service)
                .map(Service::getCoupledResources)
                .orElse(Collections.emptyList())
                .stream()
                .map(Service.CoupledResource::getIdentifier)
                .filter(cr -> !cr.isEmpty())
                .collect(Collectors.toList());
    }

    public List<Keyword> getElterProject() {
        return Optional.ofNullable(elterProject)
            .orElse(Collections.emptyList());
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
    public List<String> getWKTs() {
        return Optional.ofNullable(boundingBoxes)
                .orElse(Collections.emptyList())
                .stream()
                .map(BoundingBox::getWkt)
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
