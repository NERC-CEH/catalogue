package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.citation.Citation;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreement;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.*;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.WMS_GET_CAPABILITIES;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
        @Template(called = "html/gemini.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE),
        @Template(called = "xml/gemini.ftlx", whenRequestedAs = GEMINI_XML_VALUE),
        @Template(called = "rdf/ttl.ftl", whenRequestedAs = RDF_TTL_VALUE),
        @Template(called = "schema.org/schema.org.ftlh", whenRequestedAs = RDF_SCHEMAORG_VALUE),
        @Template(called = "ceda/ceda.ftlh", whenRequestedAs = CEDA_YAML_VALUE)
})
public class GeminiDocument extends AbstractMetadataDocument implements WellKnownText {
    private static final Set<String> ALLOWED_CITATION_FUNCTIONS = Set.of("isReferencedBy", "isSupplementTo");
    private static final String TOPIC_PROJECT_URL = "http://onto.nerc.ac.uk/CEHMD/";
    private static final Pattern WMS_ONLINE_RESOURCE = Pattern
            .compile(
                    "^https://.*/maps/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}).*",
                    CASE_INSENSITIVE
            );
    private String otherCitationDetails, lineage, reasonChanged,
            metadataStandardName, metadataStandardVersion;
    private Number version;
    private List<String> alternateTitles, spatialRepresentationTypes, temporalResolution, datasetLanguages,
            securityConstraints;
    private List<Keyword> topicCategories, keywordsDiscipline, keywordsInstrument, keywordsObservedProperty,
            keywordsPlace, keywordsProject, keywordsTheme, keywordsOther;
    private List<Geometry> geometries;
    private List<DistributionInfo> distributionFormats;
    private List<DescriptiveKeywords> descriptiveKeywords;
    private List<InspireTheme> inspireThemes;
    private List<SpatialResolution> spatialResolutions;
    private List<Funding> funding;
    private List<BoundingBox> boundingBoxes;
    private List<ResponsibleParty> distributorContacts, responsibleParties;
    private List<TimePeriod> temporalExtents;
    private List<OnlineResource> onlineResources;
    private List<SpatialReferenceSystem> spatialReferenceSystems;
    private List<Supplemental> incomingCitations, supplemental;
    @JsonIgnore
    private Citation citation;
    @JsonIgnore
    private boolean isDataciteMintable;
    @JsonIgnore
    private boolean isDatacitable;
    private DatasetReferenceDate datasetReferenceDate;
    private List<ResourceMaintenance> resourceMaintenance;
    private Service service;
    private List<ResourceConstraint> useConstraints;
    private MapDataDefinition mapDataDefinition;
    private Keyword resourceType;
    private AccessLimitation accessLimitation;
    private boolean notGEMINI;


    public void populateFromServiceAgreement(ServiceAgreement serviceAgreement) {
        this.setTitle(serviceAgreement.getTitle());
        this.setDescription(serviceAgreement.getDescription());
        this.responsibleParties = serviceAgreement.getAuthors();
        this.useConstraints = List.of(serviceAgreement.getEndUserLicence());
        this.descriptiveKeywords = serviceAgreement.getDescriptiveKeywords();
        this.lineage = serviceAgreement.getLineage();
        this.boundingBoxes = serviceAgreement.getAreaOfStudy();
        this.funding = serviceAgreement.getFunding();
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
    public GeminiDocument setType(String type) {
        super.setType(type);
        this.resourceType = Keyword.builder().value(type).build();
        return this;
    }

    @Override
    @JsonIgnore
    public List<Keyword> getAllKeywords() {
        return Stream.of(
            keywordsFromDescriptiveKeywords(),
            Optional.ofNullable(keywordsDiscipline).orElse(Collections.emptyList()),
            Optional.ofNullable(keywordsInstrument).orElse(Collections.emptyList()),
            Optional.ofNullable(keywordsObservedProperty).orElse(Collections.emptyList()),
            Optional.ofNullable(keywordsPlace).orElse(Collections.emptyList()),
            Optional.ofNullable(keywordsProject).orElse(Collections.emptyList()),
            Optional.ofNullable(keywordsTheme).orElse(Collections.emptyList()),
            Optional.ofNullable(keywordsOther).orElse(Collections.emptyList())
        )
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private List<Keyword> keywordsFromDescriptiveKeywords() {
        return Optional.ofNullable(descriptiveKeywords)
            .orElse(Collections.emptyList())
            .stream()
            .flatMap(dk -> dk.getKeywords().stream())
            .collect(Collectors.toList());
    }

    @Override
    public GeminiDocument addAdditionalKeywords(List<Keyword> additionalKeywords) {
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
    public GeminiDocument setCitation(Citation citation) {
        this.citation = citation;
        return this;
    }

    public List<OnlineResource> getOnlineResources() {
        return Optional.ofNullable(onlineResources)
            .orElse(Collections.emptyList());
    }

    @JsonIgnore
    public List<OnlineResource> getDownloads() {
        Set<String> downloadRoles = Set.of("download", "order");
        return getOnlineResources()
            .stream()
            .filter(onlineResource -> downloadRoles.contains(onlineResource.getFunction()))
            .collect(Collectors.toList());
    }

    /**
     * Return a link to the map viewer for this Gemini record if it can be
     * rendered in the map viewer
     *
     * @return The link to the map viewer if it is viewable else null
     */
    @JsonIgnore
    public String getMapViewerUrl() {
        val possibleWms = Optional.ofNullable(onlineResources)
                .orElse(Collections.emptyList())
                .stream()
                .filter(onlineResource -> onlineResource.getType().equals(WMS_GET_CAPABILITIES))
                .filter(onlineResource -> WMS_ONLINE_RESOURCE.matcher(onlineResource.getUrl()).matches())
                .findFirst();

        if (possibleWms.isPresent()) {
            val onlineResource = possibleWms.get();
            log.debug(onlineResource.toString());
            val matcher = WMS_ONLINE_RESOURCE.matcher(onlineResource.getUrl());
            log.debug("matches {}, group 1: {}", matcher.matches(), matcher.group(1));
            val id = matcher.group(1);
            return "/maps#layers/" + id;
        }
        return null;
    }

    @JsonIgnore
    public boolean isMapViewable() {
        return getMapViewerUrl() != null;
    }

    public List<String> getTopics() {
        return Optional.ofNullable(keywordsTheme)
                .orElse(Collections.emptyList())
                .stream()
                .map(Keyword::getUri)
                .filter(uri -> uri.startsWith(TOPIC_PROJECT_URL))
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

    private List<ResponsibleParty>responsiblePartyByRole(String role) {
        return getResponsibleParties()
            .stream()
            .filter((responsibleParty) -> responsibleParty.getRole().equalsIgnoreCase(role))
            .collect(Collectors.toList());
    }

    public List<ResponsibleParty> getAuthors() {
        return responsiblePartyByRole("author");
    }

    public List<ResponsibleParty> getCustodians() {
        return responsiblePartyByRole("custodian");
    }

    public List<ResponsibleParty> getPointsOfContact() {
        return responsiblePartyByRole("pointOfContact");
    }

    public List<ResponsibleParty> getRightsHolders() {
        return responsiblePartyByRole("rightsHolder");
    }

    public List<ResponsibleParty> getPublishers() {
        return responsiblePartyByRole("publisher");
    }

    public List<DistributionInfo> getDistributionFormats() {
        return Optional.ofNullable(distributionFormats)
            .orElse(Collections.emptyList());
    }

    public List<BoundingBox> getBoundingBoxes() {
        return Optional.ofNullable(boundingBoxes)
            .orElse(Collections.emptyList());
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
    public @NonNull List<String> getWKTs() {
        return Optional.ofNullable(boundingBoxes)
                .orElse(Collections.emptyList())
                .stream()
                .map(BoundingBox::getWkt)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public @NonNull List<String> getBounds() {
        return Optional.ofNullable(boundingBoxes)
                .orElse(Collections.emptyList())
                .stream()
                .map(BoundingBox::getBounds)
                .collect(Collectors.toList());
    }
    public long getIncomingCitationCount() {
        return Optional.ofNullable(incomingCitations)
            .orElse(Collections.emptyList())
            .size();
    }

}
