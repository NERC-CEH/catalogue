package uk.ac.ceh.gateway.catalogue.gemini;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.GEMINI_XML_VALUE;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.RDF_SCHEMAORG_VALUE;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.RDF_TTL_VALUE;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.WMS_GET_CAPABILITIES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.http.MediaType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.indexing.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Citation;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

@Data 
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/gemini.ftl", whenRequestedAs=MediaType.TEXT_HTML_VALUE),
    @Template(called="xml/gemini.xml.tpl",   whenRequestedAs=GEMINI_XML_VALUE),
    @Template(called="rdf/ttl.tpl",   whenRequestedAs=RDF_TTL_VALUE),
    @Template(called="schema.org/schema.org.tpl",   whenRequestedAs=RDF_SCHEMAORG_VALUE)
})
public class GeminiDocument extends AbstractMetadataDocument implements WellKnownText {
    private static final String TOPIC_PROJECT_URL = "http://onto.nerc.ac.uk/CEHMD/";
    private String otherCitationDetails, lineage, reasonChanged,
        metadataStandardName, metadataStandardVersion;
    private Number version;
    private List<String> alternateTitles, spatialRepresentationTypes, datasetLanguages,
      securityConstraints;      
    private List<Keyword> topicCategories;
    private List<DistributionInfo> distributionFormats;
    private List<DescriptiveKeywords> descriptiveKeywords;
    private List<InspireTheme> inspireThemes;
    private List<SpatialResolution> spatialResolutions;
    private List<Funding> funding;
    private List<BoundingBox> boundingBoxes;
    private List<ResponsibleParty> distributorContacts, responsibleParties;
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
    private List<ResourceMaintenance> resourceMaintenance;
    private Service service;
    private List<ResourceConstraint> useConstraints;
    private MapDataDefinition mapDataDefinition;
    private Keyword resourceType;
    private AccessLimitation accessLimitation;
    private boolean notGEMINI;

    
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
    public GeminiDocument setType(String type) {
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
     * @return The link to the map viewer if it is viewable else null
     */
    public String getMapViewerUrl() {
        if(isMapViewable()) {
            return "/maps#layers/" + getId();
        }
        return null;
    }
    
    /**
     * Decide if this gemini document has a map viewing capability. That is at 
     * least one wms is registered to this gemini document
     * @return true if a wms exists in the online resources
     */
    public boolean isMapViewable() {
        return Optional.ofNullable(onlineResources)
            .orElse(Collections.emptyList())
            .stream()
            .anyMatch((o)-> WMS_GET_CAPABILITIES == o.getType());
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
    public List<String> getWKTs() {
        return Optional.ofNullable(boundingBoxes)
            .orElse(Collections.emptyList())
            .stream()
            .map(BoundingBox::getWkt)
            .collect(Collectors.toList());
    }
}