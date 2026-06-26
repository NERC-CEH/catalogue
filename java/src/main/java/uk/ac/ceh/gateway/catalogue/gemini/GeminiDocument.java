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
import uk.ac.ceh.gateway.catalogue.geometry.BoundingBox;
import uk.ac.ceh.gateway.catalogue.geometry.Geometry;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.*;
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
        @Template(called = "html/dataResource.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE),
        @Template(called = "xml/gemini.ftlx", whenRequestedAs = GEMINI_XML_VALUE),
        @Template(called = "rdf/ttl.ftl", whenRequestedAs = RDF_TTL_VALUE),
        @Template(called = "schema.org/schema.org.ftl", whenRequestedAs = RDF_SCHEMAORG_VALUE),
        @Template(called = "croissant/croissant.ftl", whenRequestedAs = CROISSANT_VALUE),
        @Template(called = "rocrate/rocrate.ftl", whenRequestedAs = ROCRATE_VALUE),
        @Template(called = "rocrate/rocrate_attached.ftl", whenRequestedAs = ROCRATE_ATTACHED_VALUE),
})
public class GeminiDocument extends AbstractMetadataDocument implements WellKnownText {
    private static final Set<String> ALLOWED_CITATION_FUNCTIONS = Set.of("isReferencedBy", "isSupplementTo");
    private static final String TOPIC_PROJECT_URL = "http://onto.nerc.ac.uk/CEHMD/";
    private static final Pattern WMS_ONLINE_RESOURCE = Pattern
            .compile(
                    "^https://.*/maps/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}).*",
                    CASE_INSENSITIVE
            );
    private String otherCitationDetails, lineage, reasonChanged;
    private Number version;
    private List<String> alternateTitles, spatialRepresentationTypes, temporalResolution, datasetLanguages,
            securityConstraints;
    private List<Keyword> topicCategories, keywordsDiscipline, keywordsInstrument,
            keywordsPlace, keywordsProject, keywordsTheme, keywordsOther;
    private List<MetadataStandard> metadataStandards;
    private List<Geometry> geometries;
    private List<Fileset> fileset;
    private List<DistributionInfo> distributionFormats;
    private List<InspireTheme> inspireThemes;
    private List<SpatialResolution> spatialResolutions;
    private List<Funding> funding;
    private List<BoundingBox> boundingBoxes;
    private List<ResponsibleParty> distributorContacts = new ArrayList<>();
    private List<ResponsibleParty> contributors = new ArrayList<>();
    private List<ResponsibleParty> authors = new ArrayList<>();
    private List<ResponsibleParty> contactPoints = new ArrayList<>();
    private List<ResponsibleParty> publishers = new ArrayList<>();
    private List<ResponsibleParty> rightsHolders = new ArrayList<>();
    private List<ResponsibleParty> custodians = new ArrayList<>();
    private List<ResponsibleParty> otherContacts = new ArrayList<>();
    private List<TimePeriod> temporalExtents;
    private List<OnlineResource> onlineResources;
    private List<SpatialReferenceSystem> spatialReferenceSystems;
    private List<Supplemental> incomingCitations, supplemental;
    private List<AdditionalInfo> additionalInfo;
    @JsonIgnore
    private Citation citation;
    @JsonIgnore
    @Getter(onMethod_ = @JsonIgnore)
    private boolean isDataciteMintable;
    @JsonIgnore
    @Getter(onMethod_ = @JsonIgnore)
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
        this.contactPoints.add(ResponsibleParty.builder()
            .displayName(serviceAgreement.getDepositorName())
            .email(convertEmail(serviceAgreement.getDepositorContactDetails()))
            .build()
        );
        this.authors.addAll(convertEmails(serviceAgreement.getAuthors()));
        this.rightsHolders.addAll(convertEmails(serviceAgreement.getOwnersOfIpr()));
        Optional.ofNullable(serviceAgreement.getAvailability())
            .ifPresent(availability -> this.datasetReferenceDate = DatasetReferenceDate.builder()
                .releasedDate(LocalDate.parse(availability))
                .build());
        this.topicCategories = serviceAgreement.getTopicCategories();
        this.keywordsDiscipline = serviceAgreement.getKeywordsDiscipline();
        this.keywordsTheme = serviceAgreement.getKeywordsTheme();
        this.keywordsOther = serviceAgreement.getKeywordsOther();
    }

    @Data
    public static class AdditionalInfo {
        private String
            key,
            value;
    }

    @Override
    public String getType() {
        return Optional.ofNullable(resourceType)
                .map(Keyword::getValue)
                .orElse("");
    }

    public String getAvailability() {
        return Optional.ofNullable(accessLimitation)
                .map(AccessLimitation::getAvailability)
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
            Optional.ofNullable(keywordsDiscipline).orElseGet(Collections::emptyList),
            Optional.ofNullable(keywordsInstrument).orElseGet(Collections::emptyList),
            Optional.ofNullable(keywordsPlace).orElseGet(Collections::emptyList),
            Optional.ofNullable(keywordsProject).orElseGet(Collections::emptyList),
            Optional.ofNullable(keywordsTheme).orElseGet(Collections::emptyList),
            Optional.ofNullable(keywordsOther).orElseGet(Collections::emptyList)
        )
            .flatMap(Collection::stream)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public GeminiDocument addAdditionalKeywords(List<Keyword> additionalKeywords) {
        keywordsOther = Optional.ofNullable(keywordsOther)
                .orElseGet(ArrayList::new);

        if (additionalKeywords != null && !additionalKeywords.isEmpty()) {
            keywordsOther.addAll(additionalKeywords);
        }

        return this;
    }

    @JsonIgnore
    public List<ResponsibleParty> getContacts() {
        return Stream.of(
            Optional.ofNullable(otherContacts).orElseGet(Collections::emptyList),
            Optional.ofNullable(authors)
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .map(contact -> contact.withRole("author"))
                        .toList(),
            Optional.ofNullable(contributors)
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .map(contact -> contact.withRole("contributor"))
                        .toList(),
            Optional.ofNullable(contactPoints)
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .map(contact -> contact.withRole("pointOfContact"))
                        .toList(),
            Optional.ofNullable(publishers)
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .map(contact -> contact.withRole("publisher"))
                        .toList(),
            Optional.ofNullable(rightsHolders)
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .map(contact -> contact.withRole("rightsHolder"))
                        .toList(),
            Optional.ofNullable(custodians)
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .map(contact -> contact.withRole("custodian"))
                        .toList()
        )
            .flatMap(Collection::stream)
            .collect(Collectors.toCollection(ArrayList::new));
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
            .orElseGet(ArrayList::new);
    }

    @JsonIgnore
    public List<OnlineResource> getDataAccess() {
        Set<String> downloadRoles = Set.of("download", "order", "fileAccess");
        return getOnlineResources()
            .stream()
            .filter(onlineResource -> downloadRoles.contains(onlineResource.getFunction()))
            .collect(Collectors.toCollection(ArrayList::new));
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
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<String> getCoupledResources() {
        return Stream.ofNullable(service)
            .flatMap(s -> s.getCoupledResources().stream())
            .map(Service.CoupledResource::getIdentifier)
            .filter(cr -> !cr.isEmpty())
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<ResponsibleParty> getOtherContacts() {
        return Optional.ofNullable(otherContacts)
            .orElseGet(ArrayList::new);
    }

    @JsonIgnore
    private List<MetadataStandard> getCroissantConformity() {
        return Optional.ofNullable(getMetadataStandards())
            .orElseGet(Collections::emptyList).stream()
            .filter(Objects::nonNull)
            .filter(ms -> ms.getTitle().equalsIgnoreCase("Croissant Format Specification"))
            .filter(ms -> ms.getConformity().equalsIgnoreCase("Conformant"))
            //.filter(ms -> "Croissant Format Specification".equalsIgnoreCase(Optional.ofNullable(ms.getTitle()).orElse("")))
            //.filter(ms -> "Conformant".equalsIgnoreCase(Optional.ofNullable(ms.getConformity()).orElse("")))
            .collect(Collectors.toList());
    }
    @JsonIgnore
    public boolean isCroissant() {
        return !getCroissantConformity().isEmpty();
    }

    private List<ResponsibleParty> otherContactsByRole(String role) {
        return getOtherContacts()
            .stream()
            .filter(otherContacts -> otherContacts.getRole().equalsIgnoreCase(role))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<ResponsibleParty> distributorContactsByRole(String role) {
        return Optional.ofNullable(distributorContacts)
            .orElseGet(ArrayList::new)
            .stream()
            .filter(responsibleParty -> responsibleParty.getRole().equalsIgnoreCase(role))
            .collect(Collectors.toCollection(ArrayList::new));
    }


    public List<ResponsibleParty> getAuthors() {
        return new ArrayList<>(authors);
    }

    public List<ResponsibleParty> getPublishers() {
        return new ArrayList<>(publishers);
    }

    public List<ResponsibleParty> getRightsHolders() {
        return new ArrayList<>(rightsHolders);
    }

    public List<ResponsibleParty> getCustodians() {
        return new ArrayList<>(custodians);
    }

    public List<ResponsibleParty> getContributors() {
        return new ArrayList<>(contributors);
    }

    @JsonIgnore
    public List<ResponsibleParty> getDepositors() {
        return otherContactsByRole("depositor");
    }

    @JsonIgnore
    public List<ResponsibleParty> getOriginators() {
        return otherContactsByRole("originator");
    }

    @JsonIgnore
    public List<ResponsibleParty> getOwners() {
        return otherContactsByRole("owner");
    }

    @JsonIgnore
    public List<ResponsibleParty> getResourceProviders() {
        return otherContactsByRole("resourceProvider");
    }

    @JsonIgnore
    public List<ResponsibleParty> getDistributor() {
        return distributorContactsByRole("distributor");
    }

    public List<DistributionInfo> getDistributionFormats() {
        return Optional.ofNullable(distributionFormats)
            .orElseGet(ArrayList::new);
    }

    public List<BoundingBox> getBoundingBoxes() {
        return Optional.ofNullable(boundingBoxes)
            .orElseGet(ArrayList::new);
    }

    public List<Funding> getFunding() {
        return Optional.ofNullable(funding)
            .orElseGet(ArrayList::new);
    }

    public List<Supplemental> getSupplemental() {
        return Optional.ofNullable(supplemental)
            .orElseGet(ArrayList::new);
    }

    @Override
    public @NonNull List<String> getWKTs() {
        return Optional.ofNullable(boundingBoxes)
            .orElseGet(Collections::emptyList)
            .stream()
            .map(BoundingBox::getWkt)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @JsonIgnore
    public @NonNull List<String> getBounds() {
        return Optional.ofNullable(boundingBoxes)
            .orElseGet(Collections::emptyList)
            .stream()
            .map(BoundingBox::getBounds)
            .collect(Collectors.toCollection(ArrayList::new));
    }
    public long getIncomingCitationCount() {
        return Optional.ofNullable(incomingCitations)
            .map(List::size)
            .orElse(0);
    }

    public List<ResourceConstraint> getLicences() {
        return Optional.ofNullable(useConstraints)
            .orElseGet(Collections::emptyList)
            .stream().filter(resourceConstraint -> resourceConstraint.getCode().equalsIgnoreCase("license"))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<OnlineResource> getInfoLinks() {
        return Optional.ofNullable(onlineResources)
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(onlineResource -> onlineResource.getFunction().equalsIgnoreCase("information"))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private static @NonNull String convertEmail(@NonNull String email) {
        return email.endsWith("@ceh.ac.uk") ? "enquiries@ceh.ac.uk" : email;
    }

    private static @NonNull List<ResponsibleParty> convertEmails(List<ResponsibleParty> parties) {
        return Stream.ofNullable(parties)
            .flatMap(List::stream)
            .map(party -> party.withEmail(convertEmail(party.getEmail())))
            .toList();
    }

    @JsonIgnore
    public List<Supplemental> getRelatedDatasets() {
        return filterSupplemental(supplemental, "relatedDataset");
    }

    @JsonIgnore
    public List<Supplemental> getNonRelatedDatasets() {
        return excludeSupplemental(supplemental, "relatedDataset");
    }

    @JsonIgnore
    public List<OnlineResource> getBrowsingApps() {
        return filterOnlineResources(onlineResources, "browsing");
    }

    @JsonIgnore
    public boolean isEidcCustodian() {
        return Optional.ofNullable(getCustodians())
            .orElse(Collections.emptyList())
            .stream()
            .anyMatch(custodian -> "NERC EDS Environmental Information Data Centre".equals(custodian.getOrganisationName()));
    }

    @JsonIgnore
    public List<OnlineResource> getOrders() {
        return filterOnlineResources(getDataAccess(), "order");
    }

    @JsonIgnore
    public List<OnlineResource> getFileAccess() {
        return filterOnlineResources(getDataAccess(), "fileAccess");
    }

    @JsonIgnore
    public List<OnlineResource> getDownloads() {
        return filterOnlineResources(getDataAccess(), "download");
    }

    @JsonIgnore
    public List<OnlineResource> getDistributions() {
        return Stream.of(getOrders(), getFileAccess(), getDownloads())
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<OnlineResource> getExternalLinks() {
        return excludeOnlineResourcesUrl(onlineResources, ".+\\.ceh\\.ac\\.uk.+");
    }

    @JsonIgnore
    public List<OnlineResource> getMapservices() {
        return filterOnlineResourcesUrl(onlineResources, ".+catalogue\\.ceh\\.ac\\.uk\\/maps\\/.+");
    }

    @JsonIgnore
    public List<OnlineResource> getBrowseGraphics() {
        return filterOnlineResources(onlineResources, "browseGraphic");
    }

    @JsonIgnore
    public List<OnlineResource> getInformation() {
        return filterOnlineResources(onlineResources, "information");
    }

    @JsonIgnore
    public List<OnlineResource> getNonBrowseGraphics() {
        return excludeOnlineResources(onlineResources, "browseGraphic");
    }

    @JsonIgnore
    public List<OnlineResource> getSearch() {
        return filterOnlineResources(onlineResources, "search");
    }

    @JsonIgnore
    public List<ResourceConstraint> getOtherConstraints() {
        return excludeResourceConstraint(useConstraints, "license");
    }

    @JsonIgnore
    public List<ResourceConstraint> getCopyrights() {
        return filterResourceConstraint(useConstraints, "copyright");
    }

}
