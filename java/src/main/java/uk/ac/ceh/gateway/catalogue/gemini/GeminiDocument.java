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

import java.time.ZoneId;
import java.time.LocalDate;
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
    private Boolean hasOnlineServiceAgreement;

    public void populateFromServiceAgreement(ServiceAgreement serviceAgreement) {
        this.setTitle(serviceAgreement.getTitle());
        this.setDescription(serviceAgreement.getDescription());
        this.useConstraints = Optional.ofNullable(serviceAgreement.getEndUserLicence())
            .map(List::of)
            .orElse(null);
        this.lineage = serviceAgreement.getLineage();
        this.boundingBoxes = serviceAgreement.getBoundingBoxes();
        this.funding = serviceAgreement.getFunding();
        this.distributionFormats = Optional.ofNullable(serviceAgreement.getFiles())
            .map(files -> files.stream()
                .map(file -> DistributionInfo.builder()
                    .name(file.getName())
                    .type(file.getFormat())
                    .build()
                )
                .toList()
            )
            .orElse(null);
        val email = serviceAgreement.getDepositorContactDetails();
        this.responsibleParties = new ArrayList<>();
        this.responsibleParties.add(ResponsibleParty.builder()
            .individualName(serviceAgreement.getDepositorName())
            .email(email.endsWith("@ceh.ac.uk") ? "enquiries@ceh.ac.uk" : email)
            .role("pointOfContact")
            .build()
        );
        this.responsibleParties.addAll(
            Optional.ofNullable(serviceAgreement.getAuthors())
                .orElseGet(Collections::emptyList)
        );
        this.responsibleParties.addAll(
            Optional.ofNullable(serviceAgreement.getOwnersOfIpr())
                .orElseGet(Collections::emptyList)
        );
        Optional.ofNullable(serviceAgreement.getAvailability())
            .ifPresent(availability -> {
                this.datasetReferenceDate = DatasetReferenceDate.builder()
                    .releasedDate(LocalDate.parse(availability))
                    .build();
        });
        this.keywordsDiscipline = serviceAgreement.getKeywordsDiscipline();
        this.keywordsInstrument = serviceAgreement.getKeywordsInstrument();
        this.keywordsObservedProperty = serviceAgreement.getKeywordsObservedProperty();
        this.keywordsPlace = serviceAgreement.getKeywordsPlace();
        this.keywordsProject = serviceAgreement.getKeywordsProject();
        this.keywordsTheme = serviceAgreement.getKeywordsTheme();
        this.keywordsOther = serviceAgreement.getKeywordsOther();
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

    public Date getPublicationDate() {
        return Optional.ofNullable(datasetReferenceDate)
                .map(DatasetReferenceDate::getPublicationDate)
                .map(date -> Date.from(date.atStartOfDay(ZoneId.of("UTC")).toInstant()))
                .orElse(null);
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
            Optional.ofNullable(keywordsDiscipline).orElseGet(Collections::emptyList),
            Optional.ofNullable(keywordsInstrument).orElseGet(Collections::emptyList),
            Optional.ofNullable(keywordsObservedProperty).orElseGet(Collections::emptyList),
            Optional.ofNullable(keywordsPlace).orElseGet(Collections::emptyList),
            Optional.ofNullable(keywordsProject).orElseGet(Collections::emptyList),
            Optional.ofNullable(keywordsTheme).orElseGet(Collections::emptyList),
            Optional.ofNullable(keywordsOther).orElseGet(Collections::emptyList)
        )
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private List<Keyword> keywordsFromDescriptiveKeywords() {
        return Optional.ofNullable(descriptiveKeywords)
            .orElseGet(Collections::emptyList)
            .stream()
            .flatMap(dk -> dk.getKeywords().stream())
            .collect(Collectors.toList());
    }

    @Override
    public GeminiDocument addAdditionalKeywords(List<Keyword> additionalKeywords) {
        descriptiveKeywords = Optional.ofNullable(descriptiveKeywords)
            .orElseGet(ArrayList::new);

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
            .orElseGet(Collections::emptyList);
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
        return Optional.ofNullable(onlineResources)
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(onlineResource -> onlineResource.getType().equals(WMS_GET_CAPABILITIES))
            .flatMap(onlineResource -> WMS_ONLINE_RESOURCE.matcher(onlineResource.getUrl()).results())
            .findFirst()
            .map(matchResult -> "/maps#layers/" + matchResult.group(1))
            .orElse(null);
    }

    @JsonIgnore
    public boolean isMapViewable() {
        return getMapViewerUrl() != null;
    }

    public List<String> getTopics() {
        return Optional.ofNullable(keywordsTheme)
            .orElseGet(Collections::emptyList)
            .stream()
            .map(Keyword::getUri)
            .filter(uri -> uri.startsWith(TOPIC_PROJECT_URL))
            .collect(Collectors.toList());
    }

    public List<String> getCoupledResources() {
        return Stream.ofNullable(service)
            .flatMap(s -> s.getCoupledResources().stream())
            .map(Service.CoupledResource::getIdentifier)
            .filter(cr -> !cr.isEmpty())
            .collect(Collectors.toList());
    }

    public List<ResponsibleParty> getResponsibleParties() {
        return Optional.ofNullable(responsibleParties)
            .orElseGet(Collections::emptyList);
    }

    private List<ResponsibleParty> responsiblePartyByRole(String role) {
        return getResponsibleParties()
            .stream()
            .filter(responsibleParty -> responsibleParty.getRole().equalsIgnoreCase(role))
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
            .orElseGet(Collections::emptyList);
    }

    public List<BoundingBox> getBoundingBoxes() {
        return Optional.ofNullable(boundingBoxes)
            .orElseGet(Collections::emptyList);
    }

    public List<Funding> getFunding() {
        return Optional.ofNullable(funding)
            .orElseGet(Collections::emptyList);
    }

    public List<Supplemental> getSupplemental() {
        return Optional.ofNullable(supplemental)
            .orElseGet(Collections::emptyList);
    }

    @Override
    public @NonNull List<String> getWKTs() {
        return Optional.ofNullable(boundingBoxes)
            .orElseGet(Collections::emptyList)
            .stream()
            .map(BoundingBox::getWkt)
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public @NonNull List<String> getBounds() {
        return Optional.ofNullable(boundingBoxes)
            .orElseGet(Collections::emptyList)
            .stream()
            .map(BoundingBox::getBounds)
            .collect(Collectors.toList());
    }
    public long getIncomingCitationCount() {
        return Optional.ofNullable(incomingCitations)
            .map(List::size)
            .orElse(0);
    }

}
