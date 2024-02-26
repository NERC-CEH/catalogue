package uk.ac.ceh.gateway.catalogue.elter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
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
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;

import java.time.LocalDate;
import java.time.ZonedDateTime;
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
        @Template(called = "xml/iso19115.ftlx", whenRequestedAs = GEMINI_XML_VALUE),
        @Template(called = "rdf/ttl.ftlh", whenRequestedAs = RDF_TTL_VALUE),
        @Template(called = "schema.org/schema.org.ftlh", whenRequestedAs = RDF_SCHEMAORG_VALUE)
})
public class ElterDocument extends AbstractMetadataDocument implements WellKnownText {
    private static final Set<String> ALLOWED_CITATION_FUNCTIONS = Set.of("isReferencedBy", "isSupplementTo");
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
    private List<SpatialReferenceSystem> spatialReferenceSystems;
    private List<Supplemental> supplemental;
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
    private String importId;
    private ZonedDateTime importLastModified;

    public void importDataciteJson(JsonNode inputJson){
        // create document from Datacite API JSON

        // don't currently need other parts of the response
        inputJson = inputJson.get("data").get("attributes");

        // ensure title is set to something
        JsonNode jsonTitles = inputJson.get("titles");
        int numTitles = jsonTitles.size();
        if (numTitles == 0){
            this.setTitle("TITLE MISSING");
        }
        else {
            this.setTitle(jsonTitles.get(0).get("title").asText());
            ArrayList<String> alternativeTitles = new ArrayList<>();
            for (int i = 1; i < numTitles; i++){
                alternativeTitles.add(jsonTitles.get(i).get("title").asText());
            }
            this.setAlternateTitles(alternativeTitles);
        }
        // description
        StringBuilder descriptionBuilder = new StringBuilder();
        for (JsonNode node : inputJson.get("descriptions")) {
            if (!descriptionBuilder.isEmpty()) {
                descriptionBuilder.append("\n\n");
            }
            JsonNode descriptionTypeNode = node.get("descriptionType");
            if (descriptionTypeNode != null) {
                String descriptionType = descriptionTypeNode.asText();
                if (!descriptionType.equals("Other")) {
                    descriptionBuilder.append(descriptionType).append(": ");
                }
            }
            descriptionBuilder.append(node.get("description").asText().strip());
        }
        this.setDescription(descriptionBuilder.toString());
        // authors
        JsonNode jsonCreators = inputJson.get("creators").path(0);
        if (! jsonCreators.isMissingNode()){
            ResponsibleParty documentCreators = ResponsibleParty.builder()
                    .individualName(jsonCreators.get("name").asText())
                    .organisationName("Unknown")
                    .role("author")
                    .build();
            ArrayList<ResponsibleParty> list1 = new ArrayList<>();
            list1.add(documentCreators);
            this.setResponsibleParties(list1);
        }
        // onlineresources
        ArrayList<OnlineResource> list2 = new ArrayList<>();
        list2.add(
                OnlineResource.builder()
                .url(inputJson.get("url").asText())
                .name("View record")
                .description("View record at this link")
                .function("information")
                .build()
                );
        this.setOnlineResources(list2);
        // reference dates
        // the timestamp parsing is extremely dubious but we need working code NOW for the Frankfurt meeting.
        // TBF using LocalDate is totally broken anyway so it all needs redoing at some point.
        this.setDatasetReferenceDate(
                DatasetReferenceDate.builder()
                .creationDate(LocalDate.parse(inputJson.get("created").asText().substring(0,10)))
                .publicationDate(LocalDate.parse(inputJson.get("published").asText().substring(0,4) + "-01-01"))
                .creationDate(LocalDate.parse(inputJson.get("created").asText().substring(0,10)))
                .build()
                );
        // fixed stuff
        this.setAccessLimitation(
                AccessLimitation.builder()
                .value("no limitations to public access")
                .code("Available")
                .uri("http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations")
                .build()
                );
        this.setDataLevel("Level 0");
        this.setType("signpost");
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
    public @NonNull List<String> getWKTs() {
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
